package com.quokka.jobmate_connect.service;

import com.quokka.jobmate_connect.constant.*;
import com.quokka.jobmate_connect.dto.request.invatation.JobInvitationRequest;
import com.quokka.jobmate_connect.dto.request.notification.NotificationRequest;
import com.quokka.jobmate_connect.dto.response.invatation.JobInvitationResponse;
import com.quokka.jobmate_connect.entity.*;
import com.quokka.jobmate_connect.exception.AppException;
import com.quokka.jobmate_connect.exception.ErrorCode;
import com.quokka.jobmate_connect.kafka.dto.JobInvitationEvent;
import com.quokka.jobmate_connect.kafka.topic.JobInvitationEventProducer;
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

        JobInvitationMapper jobInvitationMapper;
        UserRepository userRepository;
        JobRepository jobRepository;
        WaitingListRepository waitingListRepository;
        ApplicationRepository applicationRepository;
        NotificationService notificationService;
        JobInvitationRepository jobInvitationRepository;
        JobInvitationEventProducer jobInvitationEventProducer;

        public JobInvitationResponse sendInvitation(JobInvitationRequest request) {

                UUID employerId = getCurrentUserId();
                User employer = userRepository.findById(employerId)
                                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

                User candidate = userRepository.findById(request.getCandidateId())
                                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

                WaitingList waitingList = waitingListRepository.findById(request.getWaitingListId())
                                .orElseThrow(() -> new AppException(ErrorCode.WAITING_LIST_NOT_FOUND));

                Job job = jobRepository.findById(request.getJobId())
                                .orElseThrow(() -> new AppException(ErrorCode.JOB_NOT_FOUND));

                if (jobInvitationRepository.existsByEmployer_IdAndCandidate_IdAndJob_Id(
                                employerId, candidate.getId(), job.getId())) {
                        throw new AppException(ErrorCode.INVITE_ALREADY_SENT);
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

                notificationService.sendNotification(NotificationRequest.builder()
                                .userId(candidate.getId())
                                .title("Thư mời ứng tuyển")
                                .message("Bạn nhận được lời mời ứng tuyển cho công việc: " + job.getTitle())
                                .build());

                // Publish event để gửi email
                jobInvitationEventProducer.publishInvitationEvent(JobInvitationEvent.builder()
                                .eventType("SENT")
                                .invitationId(invitation.getId())
                                .employerId(employer.getId())
                                .employerEmail(employer.getEmail())
                                .employerFullName(employer.getFullName())
                                .candidateId(candidate.getId())
                                .candidateEmail(candidate.getEmail())
                                .candidateFullName(candidate.getFullName())
                                .jobId(job.getId())
                                .jobTitle(job.getTitle())
                                .message(request.getMessage())
                                .timestamp(LocalDateTime.now())
                                .build());

                return jobInvitationMapper.toInvitation(invitation);
        }

        @Transactional
        public JobInvitationResponse acceptInvitation(UUID invitationId) {

                JobInvitation invitation = jobInvitationRepository.findById(invitationId)
                                .orElseThrow(() -> new RuntimeException("Invitation not found"));

                if (invitation.getStatus() != InvitationStatus.PENDING)
                        throw new RuntimeException("Invitation already processed");

                Job job = invitation.getJob();

                if (job.getStatus() == JobStatus.CLOSED ||
                                job.getStatus() == JobStatus.AUTO_CLOSED ||
                                job.getStatus() == JobStatus.REJECTED ||
                                job.getStatus() == JobStatus.DELETED) {
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

                notificationService.sendNotification(NotificationRequest.builder()
                                .userId(employer.getId())
                                .title("Ứng viên đã chấp nhận lời mời")
                                .message("Ứng viên " + candidate.getFullName() + " đã chấp nhận lời mời của bạn.")
                                .build());

                // Publish event để gửi email
                jobInvitationEventProducer.publishInvitationEvent(JobInvitationEvent.builder()
                                .eventType("ACCEPTED")
                                .invitationId(invitation.getId())
                                .employerId(employer.getId())
                                .employerEmail(employer.getEmail())
                                .employerFullName(employer.getFullName())
                                .candidateId(candidate.getId())
                                .candidateEmail(candidate.getEmail())
                                .candidateFullName(candidate.getFullName())
                                .jobId(job.getId())
                                .jobTitle(job.getTitle())
                                .message(invitation.getMessage())
                                .timestamp(LocalDateTime.now())
                                .build());

                return jobInvitationMapper.toInvitation(invitation);
        }

        @Transactional
        public JobInvitationResponse rejectInvitation(UUID invitationId) {

                JobInvitation invitation = jobInvitationRepository.findById(invitationId)
                                .orElseThrow(() -> new AppException(ErrorCode.INVITATION_NOT_FOUND));

                if (invitation.getStatus() != InvitationStatus.PENDING)
                        throw new AppException(ErrorCode.INVITATION_ALREADY_PROCESSED);

                invitation.setStatus(InvitationStatus.REJECTED);
                jobInvitationRepository.save(invitation);

                WaitingList wl = invitation.getWaitingList();
                wl.setStatus(RequestStatus.PENDING);
                waitingListRepository.save(wl);

                notificationService.sendNotification(NotificationRequest.builder()
                                .userId(invitation.getEmployer().getId())
                                .title("Ứng viên từ chối lời mời")
                                .message("Ứng viên " + invitation.getCandidate().getFullName() + " đã từ chối lời mời.")
                                .build());

                // Publish event để gửi email
                User employer = invitation.getEmployer();
                User candidate = invitation.getCandidate();
                Job job = invitation.getJob();
                
                jobInvitationEventProducer.publishInvitationEvent(JobInvitationEvent.builder()
                                .eventType("REJECTED")
                                .invitationId(invitation.getId())
                                .employerId(employer.getId())
                                .employerEmail(employer.getEmail())
                                .employerFullName(employer.getFullName())
                                .candidateId(candidate.getId())
                                .candidateEmail(candidate.getEmail())
                                .candidateFullName(candidate.getFullName())
                                .jobId(job.getId())
                                .jobTitle(job.getTitle())
                                .message(invitation.getMessage())
                                .timestamp(LocalDateTime.now())
                                .build());

                return jobInvitationMapper.toInvitation(invitation);
        }

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

        @Transactional
        public void expirePendingInvitationsForJob(Job job) {

                List<JobInvitation> pendingInvitations = jobInvitationRepository.findByJob_IdAndStatus(job.getId(),
                                InvitationStatus.PENDING);

                for (JobInvitation inv : pendingInvitations) {
                        inv.setStatus(InvitationStatus.EXPIRED);
                        jobInvitationRepository.save(inv);

                        notificationService.sendNotification(NotificationRequest.builder()
                                        .userId(inv.getCandidate().getId())
                                        .title("Lời mời không còn hiệu lực")
                                        .message("Công việc '" + job.getTitle()
                                                        + "' đã đóng, lời mời ứng tuyển của bạn hết hiệu lực.")
                                        .build());
                }
        }

        private UUID getCurrentUserId() {
                Jwt jwt = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
                return UUID.fromString(jwt.getClaimAsString("userId"));
        }
}
