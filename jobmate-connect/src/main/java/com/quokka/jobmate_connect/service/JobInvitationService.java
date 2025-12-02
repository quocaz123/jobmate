package com.quokka.jobmate_connect.service;

import com.quokka.jobmate_connect.constant.*;
import com.quokka.jobmate_connect.dto.request.invatation.JobInvitationRequest;
import com.quokka.jobmate_connect.dto.request.notification.NotificationRequest;
import com.quokka.jobmate_connect.dto.response.invatation.JobInvitationResponse;
import com.quokka.jobmate_connect.entity.*;
import com.quokka.jobmate_connect.mapper.JobInvitationMapper;
import com.quokka.jobmate_connect.repository.*;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class JobInvitationService {

    JobInvitationRepository jobInvitationRepository;
    JobInvitationMapper jobInvitationMapper;
    UserRepository userRepository;
    JobRepository jobRepository;
    WaitingListRepository waitingListRepository;
    ApplicationRepository applicationRepository;
    NotificationService notificationService;

    // =====================================================================
    // ⭐ GỬI LỜI MỜI
    // =====================================================================
    public JobInvitationResponse sendInvitation(JobInvitationRequest request) {

        UUID employerId = getCurrentUserId();
        User employer = userRepository.findById(employerId)
                .orElseThrow(() -> new RuntimeException("Employer not found"));

        User candidate = userRepository.findById(request.getCandidateId())
                .orElseThrow(() -> new RuntimeException("Candidate not found"));

        WaitingList waitingList = waitingListRepository.findById(request.getWaitingListId())
                .orElseThrow(() -> new RuntimeException("WaitingList not found"));

        Job job = jobRepository.findById(request.getJobId())
                .orElseThrow(() -> new RuntimeException("Job not found"));

        // ❌ Không cho phép gửi lời mời trùng 1 job + 1 candidate
        if (jobInvitationRepository.existsByEmployer_IdAndCandidate_IdAndJob_Id(
                employerId, candidate.getId(), job.getId())) {
            throw new RuntimeException("Already invited this candidate for this job");
        }

        JobInvitation invitation = JobInvitation.builder()
                .employer(employer)
                .candidate(candidate)
                .job(job)
                .waitingList(waitingList)
                .message(request.getMessage())
                .status(InvitationStatus.PENDING)
                .build();

        jobInvitationRepository.save(invitation);

        // Gửi thông báo cho candidate
        notificationService.sendNotification(NotificationRequest.builder()
                .userId(candidate.getId())
                .title("Thư mời ứng tuyển")
                .message("Bạn nhận được lời mời ứng tuyển cho công việc: " + job.getTitle())
                .build());

        return jobInvitationMapper.toInvitation(invitation);
    }

    // =====================================================================
    // ⭐ ACCEPT LỜI MỜI
    // =====================================================================
    @Transactional
    public JobInvitationResponse acceptInvitation(UUID invitationId) {

        JobInvitation invitation = jobInvitationRepository.findById(invitationId)
                .orElseThrow(() -> new RuntimeException("Invitation not found"));

        if (invitation.getStatus() != InvitationStatus.PENDING)
            throw new RuntimeException("Invitation already processed");

        Job job = invitation.getJob();

        // ❌ Không cho ACCEPT nếu job đã đóng / xoá
        if (job.getStatus() == JobStatus.CLOSED ||
                job.getStatus() == JobStatus.AUTO_CLOSED ||
                job.getStatus() == JobStatus.REJECTED) {
            throw new RuntimeException("Job is no longer available");
        }

        // 1) Update trạng thái invitation
        invitation.setStatus(InvitationStatus.ACCEPTED);
        jobInvitationRepository.save(invitation);

        // 2) WaitingList → MATCHED
        WaitingList wl = invitation.getWaitingList();
        wl.setStatus(RequestStatus.MATCHED);
        waitingListRepository.save(wl);

        User candidate = invitation.getCandidate();
        User employer = invitation.getEmployer();

        // 3) Auto tạo Application
        Application application = Application.builder()
                .job(job)
                .user(candidate)
                .status(ApplicationStatus.ACCEPTED)
                .appliedAt(LocalDateTime.now())
                .build();

        applicationRepository.save(application);

        // 4) Reject các lời mời khác của candidate
        jobInvitationRepository.findByCandidate_IdAndStatus(candidate.getId(), InvitationStatus.PENDING)
                .forEach(inv -> {
                    inv.setStatus(InvitationStatus.REJECTED);
                    jobInvitationRepository.save(inv);
                });

        // 5) Thông báo cho employer
        notificationService.sendNotification(NotificationRequest.builder()
                .userId(employer.getId())
                .title("Ứng viên đã chấp nhận lời mời")
                .message("Ứng viên " + candidate.getFullName() + " đã chấp nhận lời mời của bạn.")
                .build());

        return jobInvitationMapper.toInvitation(invitation);
    }

    // =====================================================================
    // ⭐ REJECT LỜI MỜI
    // =====================================================================
    @Transactional
    public JobInvitationResponse rejectInvitation(UUID invitationId) {

        JobInvitation invitation = jobInvitationRepository.findById(invitationId)
                .orElseThrow(() -> new RuntimeException("Invitation not found"));

        if (invitation.getStatus() != InvitationStatus.PENDING)
            throw new RuntimeException("Invitation already processed");

        invitation.setStatus(InvitationStatus.REJECTED);
        jobInvitationRepository.save(invitation);

        // WaitingList → AVAILABLE
        WaitingList wl = invitation.getWaitingList();
        wl.setStatus(RequestStatus.PENDING);
        waitingListRepository.save(wl);

        // Gửi thông báo cho employer
        notificationService.sendNotification(NotificationRequest.builder()
                .userId(invitation.getEmployer().getId())
                .title("Ứng viên từ chối lời mời")
                .message("Ứng viên " + invitation.getCandidate().getFullName() + " đã từ chối lời mời.")
                .build());

        return jobInvitationMapper.toInvitation(invitation);
    }

    // =====================================================================
    // ⭐ Danh sách lời mời
    // =====================================================================
    public List<JobInvitationResponse> getMyInvitations() {
        UUID userId = getCurrentUserId();
        return jobInvitationRepository.findByCandidate_IdOrderByCreatedAtDesc(userId)
                .stream().map(jobInvitationMapper::toInvitation).toList();
    }

    public List<JobInvitationResponse> getSentInvitations() {
        UUID userId = getCurrentUserId();
        return jobInvitationRepository.findByEmployer_IdOrderByCreatedAtDesc(userId)
                .stream().map(jobInvitationMapper::toInvitation).toList();
    }

    private UUID getCurrentUserId() {
        Jwt jwt = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return UUID.fromString(jwt.getClaimAsString("userId"));
    }
}
