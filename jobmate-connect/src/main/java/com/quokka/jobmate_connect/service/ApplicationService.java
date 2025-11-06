package com.quokka.jobmate_connect.service;

import com.quokka.jobmate_connect.constant.ApplicationStatus;
import com.quokka.jobmate_connect.constant.FileTypeStatus;
import com.quokka.jobmate_connect.constant.JobStatus;
import com.quokka.jobmate_connect.dto.PageResponse;
import com.quokka.jobmate_connect.dto.request.application.ApplicationRequest;
import com.quokka.jobmate_connect.dto.request.notification.NotificationRequest;
import com.quokka.jobmate_connect.dto.response.application.ApplicationResponse;
import com.quokka.jobmate_connect.dto.response.file.FileResponse;
import com.quokka.jobmate_connect.entity.Application;
import com.quokka.jobmate_connect.entity.Job;
import com.quokka.jobmate_connect.entity.User;
import com.quokka.jobmate_connect.exception.AppException;
import com.quokka.jobmate_connect.exception.ErrorCode;
import com.quokka.jobmate_connect.mapper.ApplicationMapper;
import com.quokka.jobmate_connect.repository.ApplicationRepository;
import com.quokka.jobmate_connect.repository.JobRepository;
import com.quokka.jobmate_connect.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ApplicationService {
    ApplicationRepository applicationRepository;
    ApplicationMapper applicationMapper;
    UserRepository userRepository;
    JobRepository jobRepository;
    NotificationService notificationService;
    FileService fileService;

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email).orElseThrow(
                () -> new AppException(ErrorCode.USER_NOT_FOUND)
        );
    }

    // Ứng viên nộp đơn vào một công việc.
    @Transactional
    public ApplicationResponse applyJob(ApplicationRequest request) {
        User user = getCurrentUser();

        Job job = jobRepository.findById(request.getJobId()).orElseThrow(
                () -> new AppException(ErrorCode.JOB_NOT_FOUND)
        );

        if(job.getStatus() != JobStatus.APPROVED) {
            throw new AppException(ErrorCode.JOB_NOT_AVAILABLE);
        }

        if(applicationRepository.existsByJobIdAndUserId(job.getId(), user.getId())) {
            throw new AppException(ErrorCode.ALREADY_APPLIED);
        }

        String resumeUrl = null;
        if(request.getResumeUrl() != null) {
            try {
                FileResponse fileResponse = fileService.uploadFile(request.getResumeFile(), FileTypeStatus.RESUME);
                resumeUrl = fileResponse.getUrl();
            } catch (IOException e) {
                throw new AppException(ErrorCode.FILE_UPLOAD_FAILED);
            }
        }

        Application application = Application.builder()
                .job(job)
                .user(user)
                .status(ApplicationStatus.PENDING)
                .coverLetter(request.getCoverLetter())
                .resumeUrl(resumeUrl)
                .build();


        notificationService.sendNotification(NotificationRequest.builder()
                        .userId(job.getCreatedBy().getId())
                .title("New Job Application")
                .message("You have received a new application for your job posting: " + job.getTitle())
                .build()
        );

        applicationRepository.save(application);

        Long appCount = applicationRepository.countByJobId(job.getId());
        job.setApplicationCount(appCount != null ? appCount.intValue() : 0);
        jobRepository.save(job);

        return applicationMapper.toApplicationResponse(application);
    }

    // Ứng viên xem danh sách các đơn ứng tuyển của chính mình.
    public PageResponse<ApplicationResponse> getMyApplications(int page, int size) {
        User user = getCurrentUser();
        Pageable pageable = PageRequest.of(page, size);
        Page<Application> applicationPage = applicationRepository.findByUserIdOrderByAppliedAtDesc(user.getId(), pageable);

        return PageResponse.<ApplicationResponse>builder()
                .currentPage(applicationPage.getNumber())
                .pageSize(size)
                .totalElements(applicationPage.getTotalElements())
                .totalPages(applicationPage.getTotalPages())
                .data(applicationPage.getContent()
                        .stream()
                        .map(applicationMapper::toApplicationResponse)
                        .toList())
                .build();
    }

    // Nhà tuyển dụng xem danh sách ứng viên nộp vào job của mình.
    public PageResponse<ApplicationResponse> getJobApplications(int page, int size, UUID jobId) {
        User user = getCurrentUser();

        Job job = jobRepository.findById(jobId).orElseThrow(
                () -> new AppException(ErrorCode.JOB_NOT_FOUND)
        );

        if(!job.getCreatedBy().getId().equals(user.getId())) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<Application> applications = applicationRepository.findByJobIdOrderByAppliedAtDesc(jobId, pageable);

        return PageResponse.<ApplicationResponse>builder()
                .currentPage(applications.getNumber())
                .pageSize(size)
                .totalElements(applications.getTotalElements())
                .totalPages(applications.getTotalPages())
                .data(applications.getContent()
                        .stream()
                        .map(applicationMapper::toApplicationResponse)
                        .toList())
                .build();
    }

    // Nhà tuyển dụng duyệt / từ chối / hủy đơn ứng tuyển.
    @Transactional
    public ApplicationResponse updateApplicationStatus(UUID applicationId, ApplicationStatus status, String reason) {
        Application application = applicationRepository.findById(applicationId).orElseThrow(
                () -> new AppException(ErrorCode.APPLICATION_NOT_FOUND));

        User user = getCurrentUser();

        if(!application.getJob().getCreatedBy().getId().equals(user.getId())) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        application.setStatus(status);
        if(status == ApplicationStatus.REJECTED) {
            application.setRejectionReason(reason);
        }
        if(status == ApplicationStatus.CANCELLED) {
            application.setCancelledAt(LocalDateTime.now());
        }

        application = applicationRepository.save(application);

        notificationService.sendNotification(NotificationRequest.builder()
                        .userId(application.getUser().getId())
                .title("Cập nhật trạng thái đơn ứng tuyển")
                .message("Đơn ứng tuyển của bạn cho công việc: " + application.getJob().getTitle() +
                        " đã được " + switch (status) {
                            case ACCEPTED -> "chấp nhận.";
                            case REJECTED -> "từ chối" + (reason != null ? ". Lý do: " + reason : "") + ".";
                            case CANCELLED -> "hủy bởi nhà tuyển dụng.";
                            default -> "cập nhật.";
                })
                .build()
        );

        Job job = application.getJob();
        Long appCount = applicationRepository.countByJobId(job.getId());
        job.setApplicationCount(appCount != null ? appCount.intValue() : 0);
        jobRepository.save(job);

        return applicationMapper.toApplicationResponse(application);
    }

    // Ứng viên hủy đơn của chính mình.
    @Transactional
    public void cancelApplication(UUID applicationId) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new AppException(ErrorCode.APPLICATION_NOT_FOUND));

        User user = getCurrentUser();

        if(!application.getUser().getId().equals(user.getId())) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        if(application.getStatus() != ApplicationStatus.PENDING) {
            throw new AppException(ErrorCode.CANNOT_CANCEL_APPLICATION);
        }

        application.setStatus(ApplicationStatus.CANCELLED);
        application.setCancelledAt(LocalDateTime.now());
        applicationRepository.save(application);

        Job job = application.getJob();
        Long appCount = applicationRepository.countByJobId(job.getId());
        job.setApplicationCount(appCount != null ? appCount.intValue() : 0);
        jobRepository.save(job);
    }


}
