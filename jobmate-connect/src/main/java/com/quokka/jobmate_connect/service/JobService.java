package com.quokka.jobmate_connect.service;

import com.quokka.jobmate_connect.constant.JobStatus;
import com.quokka.jobmate_connect.constant.NotificationType;
import com.quokka.jobmate_connect.dto.ApiResponse;
import com.quokka.jobmate_connect.dto.PageResponse;
import com.quokka.jobmate_connect.dto.request.job.JobCreationRequest;
import com.quokka.jobmate_connect.dto.request.notification.NotificationRequest;
import com.quokka.jobmate_connect.dto.response.job.JobResponse;
import com.quokka.jobmate_connect.entity.Job;
import com.quokka.jobmate_connect.entity.User;
import com.quokka.jobmate_connect.exception.AppException;
import com.quokka.jobmate_connect.exception.ErrorCode;
import com.quokka.jobmate_connect.mapper.JobMapper;
import com.quokka.jobmate_connect.repository.JobRepository;
import com.quokka.jobmate_connect.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class JobService {
    JobRepository jobRepository;
    JobMapper jobMapper;
    GeocodingService geocodingService;
    NotificationService notificationService;


    static final double EARTH_RADIUS_KM = 6371.0;
    private final UserRepository userRepository;

    @Transactional
    public JobResponse createJob(JobCreationRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Jwt jwt = (Jwt) auth.getPrincipal();
        UUID userId = UUID.fromString(jwt.getClaim("userId"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        double[] coordinates = geocodingService.getCoordinates(request.getLocation());

        Job job = Job.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .location(request.getLocation())
                .latitude(coordinates[0])
                .longitude(coordinates[1])
                .salary(request.getSalary())
                .jobType(request.getJobType())
                .skills(request.getSkills())
                .status(JobStatus.PENDING_REVIEW)
                .createdBy(user)
                .startAt(request.getStartAt())
                .deadline(request.getDeadline())
                .createdAt(LocalDateTime.now())
                .build();

        jobRepository.save(job);

        notificationService.notifyAdmins("Yêu cầu duyệt công việc mới",
                "Người dùng " + user.getFullName() +
                        " đã tạo công việc mới: '" + job.getTitle() +
                        "'. Vui lòng xem xét và phê duyệt." );

        notificationService.sendNotification(NotificationRequest.builder()
                .userId(userId)
                .title("Đăng công việc thành công")
                .message("Công việc '" + job.getTitle() + "' đã được tạo và đang chờ phê duyệt.")
                .type(NotificationType.SYSTEM)
                .build());

        return jobMapper.toJobResponse(job);
    }

    public JobResponse getJobDetails(UUID jobId) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new AppException(ErrorCode.JOB_NOT_FOUND));

        if(job.getStatus() != JobStatus.APPROVED) {
            throw new AppException(ErrorCode.JOB_NOT_FOUND);
        }

        return jobMapper.toJobResponse(job);
    }

    public PageResponse<JobResponse> getAllJobs(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Job> jobPage = jobRepository.findAll(pageable);

        return PageResponse.<JobResponse>builder()
                .currentPage(page + 1)
                .totalPages(jobPage.getTotalPages())
                .pageSize(size)
                .totalElements(jobPage.getTotalElements())
                .data(jobPage.map(jobMapper::toJobResponse).getContent())
                .build();
    }

    public PageResponse<JobResponse> getMyJobs(int page, int size) {
        Jwt auth = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UUID userId = UUID.fromString(auth.getClaim("userId"));

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Job> jobPage = jobRepository.findByCreatedById(userId, pageable);

        return PageResponse.<JobResponse>builder()
                .currentPage(page)
                .totalPages(jobPage.getTotalPages())
                .pageSize(size)
                .totalElements(jobPage.getTotalElements())
                .data(jobPage.map(jobMapper::toJobResponse).getContent())
                .build();
    }

    public PageResponse<JobResponse> getNearByJob(double radiusKm, int page, int size) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Jwt jwt = (Jwt) auth.getPrincipal();
        UUID userId = UUID.fromString(jwt.getClaim("userId"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        Pageable pageable = PageRequest.of(page, size);
        Page<Job> jobPage = jobRepository.findByStatus(JobStatus.APPROVED, pageable);

        List<JobResponse> nearby = jobPage.stream()
                .map(job -> {
                    double distance = geocodingService.calculateDistance(
                            user.getLatitude(), user.getLongitude(),
                            job.getLatitude(), job.getLongitude());

                    return JobResponse.builder()
                            .id(job.getId())
                            .title(job.getTitle())
                            .description(job.getDescription())
                            .location(job.getLocation())
                            .latitude(job.getLatitude())
                            .longitude(job.getLongitude())
                            .salary(job.getSalary())
                            .jobType(job.getJobType())
                            .skills(job.getSkills())
                            .status(job.getStatus())
                            .createdByName(job.getCreatedBy().getFullName())
                            .createdAt(job.getCreatedAt())
                            .deadline(job.getDeadline())
                            .distance(distance)
                            .build();
                })
                .filter(j -> j.getDistance() <= radiusKm)
                .sorted(Comparator.comparing(JobResponse::getDistance))
                .toList();

        return PageResponse.<JobResponse>builder()
                .currentPage(page + 1)
                .pageSize(size)
                .totalPages(jobPage.getTotalPages())
                .totalElements(nearby.size())
                .data(nearby)
                .build();
    }

    @Transactional
    public void updateJobVerificationStatus(UUID jobId, JobStatus status, String reason) {
        Jwt auth = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UUID userId = UUID.fromString(auth.getClaim("userId"));


        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found"));

        String message = null;

        if(status == JobStatus.APPROVED) {
            job.setStatus(JobStatus.APPROVED);
            message = message = "Công việc '" + job.getTitle() + "' của bạn đã được duyệt và hiển thị trên hệ thống.";
        } else if(status == JobStatus.REJECTED) {
            job.setStatus(JobStatus.REJECTED);
            job.setRejectionReason(reason);
            message = "Công việc '" + job.getTitle() + "' đã bị từ chối duyệt. Lý do: " + reason;
        }


        job.setVerifiedBy(userId);
        job.setVerifiedAt(LocalDateTime.now());
        job.setUpdatedAt(LocalDateTime.now());
        jobRepository.save(job);

        notificationService.sendNotification(NotificationRequest.builder()
                        .userId(job.getCreatedBy().getId())
                        .title(status == JobStatus.APPROVED ? "Công việc đã được duyệt" : "Công việc bị từ chối")
                        .message(message)
                .build());
    }

    @Transactional
    public JobResponse updateJob(UUID jobId, JobCreationRequest request) {
        Jwt auth = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UUID userId = UUID.fromString(auth.getClaim("userId"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new AppException(ErrorCode.JOB_NOT_FOUND));

        if(!job.getCreatedBy().getId().equals(user.getId())) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        if (job.getStatus() != JobStatus.REJECTED && job.getStatus() != JobStatus.CLOSED) {
            throw new AppException(ErrorCode.JOB_CANNOT_BE_UPDATED);
        }

        job.setTitle(request.getTitle());
        job.setDescription(request.getDescription());
        job.setLocation(request.getLocation());
        job.setSalary(request.getSalary());
        job.setJobType(request.getJobType());
        job.setSkills(request.getSkills());
        job.setStartAt(request.getStartAt());
        job.setDeadline(request.getDeadline());


        double[] coordinates = geocodingService.getCoordinates(request.getLocation());
        job.setLatitude(coordinates[0]);
        job.setLongitude(coordinates[1]);

        job.setStatus(JobStatus.PENDING_REVIEW);

        job.setUpdatedAt(LocalDateTime.now());
        jobRepository.save(job);

        notificationService.notifyAdmins(
                "Yêu cầu duyệt lại công việc",
                "Người dùng " + user.getFullName() + " đã chỉnh sửa công việc '" + job.getTitle() +
                        "' và gửi lại để duyệt."
        );

        return jobMapper.toJobResponse(job);
    }

    public PageResponse<JobResponse> getAvailableJobs(int page, int size, String keyword, String location) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Job> jobPage;

        if (keyword != null && !keyword.isEmpty()) {
            jobPage = jobRepository.findByTitleContainingIgnoreCaseAndStatus(keyword, JobStatus.APPROVED, pageable);
        } else if (location != null && !location.isEmpty()) {
            jobPage = jobRepository.findByLocationContainingIgnoreCaseAndStatus(location, JobStatus.APPROVED, pageable);
        } else {
            jobPage = jobRepository.findByStatus(JobStatus.APPROVED, pageable);
        }

        return PageResponse.<JobResponse>builder()
                .currentPage(page + 1)
                .totalPages(jobPage.getTotalPages())
                .pageSize(size)
                .totalElements(jobPage.getTotalElements())
                .data(jobPage.map(jobMapper::toJobResponse).getContent())
                .build();
    }
}
