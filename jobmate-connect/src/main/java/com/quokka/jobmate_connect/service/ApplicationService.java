package com.quokka.jobmate_connect.service;

import com.quokka.jobmate_connect.constant.ApplicationStatus;
import com.quokka.jobmate_connect.constant.AuditAction;
import com.quokka.jobmate_connect.constant.FileTypeStatus;
import com.quokka.jobmate_connect.constant.JobStatus;
import com.quokka.jobmate_connect.dto.PageResponse;
import com.quokka.jobmate_connect.dto.request.application.ApplicationRequest;
import com.quokka.jobmate_connect.dto.request.notification.NotificationRequest;
import com.quokka.jobmate_connect.dto.response.application.ApplicationDetailResponse;
import com.quokka.jobmate_connect.dto.response.application.ApplicationListResponse;
import com.quokka.jobmate_connect.dto.response.application.ApplicationResponse;
import com.quokka.jobmate_connect.entity.Application;
import com.quokka.jobmate_connect.entity.Job;
import com.quokka.jobmate_connect.entity.User;
import com.quokka.jobmate_connect.kafka.dto.ApplicationCreatedEvent;
import com.quokka.jobmate_connect.kafka.dto.ApplicationStatusUpdatedEvent;
import com.quokka.jobmate_connect.kafka.topic.ApplicationEventProducer;
import com.quokka.jobmate_connect.exception.AppException;
import com.quokka.jobmate_connect.exception.ErrorCode;
import com.quokka.jobmate_connect.mapper.ApplicationMapper;
import com.quokka.jobmate_connect.repository.ApplicationRepository;
import com.quokka.jobmate_connect.repository.FileMgtRepository;
import com.quokka.jobmate_connect.repository.JobRepository;
import com.quokka.jobmate_connect.repository.UserRepository;
import com.quokka.jobmate_connect.service.maching.MatchingService;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ApplicationService {

    ApplicationRepository applicationRepository;
    ApplicationMapper applicationMapper;
    UserRepository userRepository;
    JobRepository jobRepository;
    NotificationService notificationService;
    FileService fileService;
    FileMgtRepository fileMgtRepository;
    MatchingService matchingService;
    AuditLogService auditLogService;
    ApplicationEventProducer applicationEventProducer;

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
    }

    private UUID getCurrentUserId() {
        Jwt jwt = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return UUID.fromString(jwt.getClaimAsString("userId"));
    }

    private void updateJobApplicationCount(Job job) {
        Long appCount = applicationRepository.countByJobId(job.getId());
        int totalApplications = appCount != null ? appCount.intValue() : 0;
        job.setApplicationCount(totalApplications);

        // Đếm số lượng applications đã được chấp nhận (ACCEPTED)
        Long acceptedCount = applicationRepository.countAcceptedByJobId(job.getId());
        int acceptedApplications = acceptedCount != null ? acceptedCount.intValue() : 0;

        boolean autoClosed = false;
        Integer targetApplicants = job.getTargetApplicants();
        // Chỉ đóng job khi đủ số lượng ứng viên đã được chấp nhận (ACCEPTED)
        if (targetApplicants != null && targetApplicants > 0
                && acceptedApplications >= targetApplicants
                && job.getStatus() != JobStatus.CLOSED
                && job.getStatus() != JobStatus.AUTO_CLOSED) {
            job.setStatus(JobStatus.CLOSED);
            job.setUpdatedAt(LocalDateTime.now());
            autoClosed = true;
        }

        jobRepository.save(job);

        if (autoClosed) {
            notificationService.sendNotification(NotificationRequest.builder()
                    .userId(job.getCreatedBy().getId())
                    .title("Công việc đã được đóng")
                    .message("Công việc '" + job.getTitle() + "' đã tự động đóng vì đủ số lượng ứng viên đã chấp nhận.")
                    .build());
            auditLogService.record((User) null, AuditAction.JOB_STATUS_CHANGE, job.getId(),
                    job.getTitle(),
                    "Tự động đóng khi đạt " + acceptedApplications + " ứng viên đã chấp nhận");
        }
    }

    private void ensureJobAvailable(Job job) {
        if (job.getStatus() == JobStatus.CLOSED ||
                job.getStatus() == JobStatus.REJECTED ||
                job.getStatus() == JobStatus.DELETED) {
            throw new AppException(ErrorCode.JOB_NOT_AVAILABLE);
        }

        Integer targetApplicants = job.getTargetApplicants();
        if (targetApplicants == null || targetApplicants <= 0) {
            return;
        }

        // Chỉ kiểm tra số lượng applications đã được chấp nhận (ACCEPTED)
        Long acceptedCount = applicationRepository.countAcceptedByJobId(job.getId());
        if (acceptedCount != null && acceptedCount.intValue() >= targetApplicants) {
            throw new AppException(ErrorCode.JOB_NOT_AVAILABLE);
        }
    }

    @Transactional
    public ApplicationResponse applyJob(ApplicationRequest request) {
        UUID userId = getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        // Kiểm tra user có bị banned không
        if ("BANNED".equalsIgnoreCase(user.getStatus())) {
            throw new AppException(ErrorCode.USER_BANNED);
        }

        Job job = jobRepository.findById(request.getJobId())
                .orElseThrow(() -> new AppException(ErrorCode.JOB_NOT_FOUND));

        // Kiểm tra employer có bị banned không
        User employer = job.getCreatedBy();
        if (employer != null && "BANNED".equalsIgnoreCase(employer.getStatus())) {
            throw new AppException(ErrorCode.JOB_NOT_AVAILABLE);
        }

        ensureJobAvailable(job);

        applicationRepository.findByUserIdAndJobId(userId, job.getId()).ifPresent(existingApp -> {
            if (existingApp.getStatus() == ApplicationStatus.PENDING ||
                    existingApp.getStatus() == ApplicationStatus.ACCEPTED) {
                throw new AppException(ErrorCode.ALREADY_APPLIED);
            } else if (existingApp.getStatus() == ApplicationStatus.REJECTED ||
                    existingApp.getStatus() == ApplicationStatus.CANCELLED) {
                applicationRepository.delete(existingApp);
                log.info("🗑 Đã xóa đơn cũ của [{}] để apply lại job [{}]", user.getEmail(), job.getTitle());
            }
        });

        boolean hasResume = false;
        String resumeFileName = null;
        MultipartFile resumeFile = request.getResumeFile();

        // Upload CV mới
        if (resumeFile != null && !resumeFile.isEmpty()) {
            try {
                var upload = fileService.uploadFile(resumeFile, FileTypeStatus.RESUME);
                hasResume = true;
                resumeFileName = upload.getUrl().substring(upload.getUrl().lastIndexOf("/") + 1);
            } catch (Exception e) {
                log.error("Upload resume failed for user {}", user.getEmail(), e);
                throw new AppException(ErrorCode.INTERNAL_ERROR);
            }
        } else if (request.isUseProfileResume()) {
            resumeFileName = fileMgtRepository.findByOwnerIdAndType(userId, FileTypeStatus.RESUME)
                    .map(file -> file.getUrl().substring(file.getUrl().lastIndexOf("/") + 1))
                    .orElse(null);
            if (resumeFileName != null) {
                hasResume = true;
            }
        }

        Application application = Application.builder()
                .user(user)
                .job(job)
                .coverLetter(request.getCoverLetter())
                .hasResume(hasResume)
                .resumeFileName(resumeFileName)
                .status(ApplicationStatus.PENDING)
                .appliedAt(LocalDateTime.now())
                .build();

        applicationRepository.save(application);
        updateJobApplicationCount(job);

        double matchScore = matchingService.calculateMatchScore(user, job);

        notificationService.sendNotification(NotificationRequest.builder()
                .userId(employer.getId())
                .title("Đơn ứng tuyển mới")
                .message("Bạn vừa nhận được một đơn ứng tuyển mới cho công việc: " + job.getTitle())
                .build());

        // Publish event để gửi email cho employer
        applicationEventProducer.publishApplicationCreatedEvent(ApplicationCreatedEvent.builder()
                .applicationId(application.getId())
                .candidateId(user.getId())
                .candidateEmail(user.getEmail())
                .candidateFullName(user.getFullName())
                .employerId(employer.getId())
                .employerEmail(employer.getEmail())
                .employerFullName(employer.getFullName())
                .jobId(job.getId())
                .jobTitle(job.getTitle())
                .coverLetter(application.getCoverLetter())
                .hasResume(application.isHasResume())
                .resumeFileName(application.getResumeFileName())
                .matchScore(matchScore)
                .appliedAt(application.getAppliedAt())
                .timestamp(LocalDateTime.now())
                .build());

        auditLogService.record(user, AuditAction.APPLICATION_CREATE, application.getId(),
                job.getTitle(), "Ứng viên: " + user.getFullName());

        ApplicationResponse res = applicationMapper.toApplicationResponse(application);
        res.setMatchScore(matchScore);
        return res;
    }

    public PageResponse<ApplicationListResponse> getMyApplications(int page, int size) {
        UUID userId = getCurrentUserId();
        Pageable pageable = PageRequest.of(page, size);
        Page<Application> apps = applicationRepository.findByUserIdOrderByAppliedAtDesc(userId, pageable);

        return PageResponse.<ApplicationListResponse>builder()
                .currentPage(apps.getNumber())
                .pageSize(size)
                .totalElements(apps.getTotalElements())
                .totalPages(apps.getTotalPages())
                .data(apps.getContent()
                        .stream()
                        .map(app -> {
                            double score = 0.0;
                            try {
                                score = matchingService.calculateMatchScore(app.getUser(), app.getJob());
                            } catch (Exception e) {
                                log.warn("⚠ Không thể tính matchScore cho {}", app.getUser().getEmail());
                            }
                            ApplicationListResponse dto = applicationMapper.toListResponse(app);
                            dto.setMatchScore(score);
                            return dto;
                        })
                        .toList())
                .build();
    }

    public PageResponse<ApplicationListResponse> getJobApplications(
            int page, int size, UUID jobId, ApplicationStatus status) {
        UUID recruiterId = getCurrentUserId();

        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new AppException(ErrorCode.JOB_NOT_FOUND));

        if (!job.getCreatedBy().getId().equals(recruiterId)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<Application> applications;

        if (status != null) {
            applications = applicationRepository.findByJobIdAndStatusOrderByAppliedAtDesc(jobId, status, pageable);
        } else {
            applications = applicationRepository.findByJobIdOrderByAppliedAtDesc(jobId, pageable);
        }

        return PageResponse.<ApplicationListResponse>builder()
                .currentPage(applications.getNumber())
                .pageSize(size)
                .totalElements(applications.getTotalElements())
                .totalPages(applications.getTotalPages())
                .data(applications.getContent().stream().map(app -> {
                    double score = 0.0;
                    try {
                        score = matchingService.calculateMatchScore(app.getUser(), app.getJob());
                    } catch (Exception e) {
                        log.warn("Không thể tính matchScore cho {}", app.getUser().getEmail());
                    }

                    ApplicationListResponse dto = applicationMapper.toListResponse(app);
                    dto.setMatchScore(score);
                    dto.setStatus(app.getStatus());
                    dto.setAppliedAt(app.getAppliedAt());
                    return dto;
                }).toList())
                .build();
    }

    @Transactional
    public ApplicationResponse updateApplicationStatus(UUID applicationId, ApplicationStatus status, String reason) {
        Application app = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new AppException(ErrorCode.APPLICATION_NOT_FOUND));

        User recruiter = getCurrentUser();
        if (!app.getJob().getCreatedBy().getId().equals(recruiter.getId())) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        app.setStatus(status);
        if (status == ApplicationStatus.REJECTED)
            app.setRejectionReason(reason);
        if (status == ApplicationStatus.CANCELLED)
            app.setCancelledAt(LocalDateTime.now());

        applicationRepository.save(app);
        updateJobApplicationCount(app.getJob());

        String message = switch (status) {
            case ACCEPTED -> "Đơn ứng tuyển của bạn đã được chấp nhận.";
            case REJECTED -> "Đơn ứng tuyển của bạn đã bị từ chối." +
                    (reason != null ? " Lý do: " + reason : "");
            case CANCELLED -> "Đơn ứng tuyển của bạn đã bị hủy bởi nhà tuyển dụng.";
            default -> "Đơn ứng tuyển của bạn đã được cập nhật.";
        };

        notificationService.sendNotification(NotificationRequest.builder()
                .userId(app.getUser().getId())
                .title("Cập nhật trạng thái đơn ứng tuyển")
                .message(message)
                .build());

        // Publish event để gửi email cho candidate
        User candidate = app.getUser();
        User employer = app.getJob().getCreatedBy();
        Job job = app.getJob();

        applicationEventProducer.publishApplicationStatusUpdatedEvent(ApplicationStatusUpdatedEvent.builder()
                .applicationId(app.getId())
                .status(status.name())
                .candidateId(candidate.getId())
                .candidateEmail(candidate.getEmail())
                .candidateFullName(candidate.getFullName())
                .employerId(employer.getId())
                .employerEmail(employer.getEmail())
                .employerFullName(employer.getFullName())
                .jobId(job.getId())
                .jobTitle(job.getTitle())
                .reason(reason)
                .updatedAt(LocalDateTime.now())
                .timestamp(LocalDateTime.now())
                .build());

        String detail = reason != null && !reason.isBlank()
                ? "Trạng thái: " + status.name() + " - Lý do: " + reason
                : "Trạng thái: " + status.name();
        auditLogService.record(recruiter, AuditAction.APPLICATION_UPDATE_STATUS, app.getId(),
                app.getJob().getTitle() + " - " + app.getUser().getFullName(),
                detail);

        double score = matchingService.calculateMatchScore(app.getUser(), app.getJob());
        ApplicationResponse res = applicationMapper.toApplicationResponse(app);
        res.setMatchScore(score);
        return res;
    }

    @Transactional
    public void cancelApplication(UUID applicationId) {
        Application app = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new AppException(ErrorCode.APPLICATION_NOT_FOUND));

        User user = getCurrentUser();
        if (!app.getUser().getId().equals(user.getId())) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        if (app.getStatus() != ApplicationStatus.PENDING) {
            throw new AppException(ErrorCode.CANNOT_CANCEL_APPLICATION);
        }

        app.setStatus(ApplicationStatus.CANCELLED);
        app.setCancelledAt(LocalDateTime.now());
        applicationRepository.save(app);
        updateJobApplicationCount(app.getJob());
        auditLogService.record(user, AuditAction.APPLICATION_CANCEL, app.getId(),
                app.getJob().getTitle(), "Ứng viên hủy đơn");
    }

    @Transactional
    public ApplicationDetailResponse getApplicationDetail(UUID applicationId) {
        UUID currentUserId = getCurrentUserId();
        Application app = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new AppException(ErrorCode.APPLICATION_NOT_FOUND));

        if (!app.getUser().getId().equals(currentUserId)
                && !app.getJob().getCreatedBy().getId().equals(currentUserId)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        double score = matchingService.calculateMatchScore(app.getUser(), app.getJob());
        ApplicationDetailResponse res = applicationMapper.toDetailResponse(app);
        res.setMatchScore(score);
        return res;
    }
}
