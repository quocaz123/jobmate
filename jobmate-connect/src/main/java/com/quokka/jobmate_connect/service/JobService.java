package com.quokka.jobmate_connect.service;

import com.quokka.jobmate_connect.constant.JobStatus;
import com.quokka.jobmate_connect.constant.NotificationType;
import com.quokka.jobmate_connect.dto.ApiResponse;
import com.quokka.jobmate_connect.dto.PageResponse;
import com.quokka.jobmate_connect.dto.request.job.JobCreationRequest;
import com.quokka.jobmate_connect.dto.request.notification.NotificationRequest;
import com.quokka.jobmate_connect.dto.response.job.JobDetailResponse;
import com.quokka.jobmate_connect.dto.response.job.JobResponse;
import com.quokka.jobmate_connect.entity.Job;
import com.quokka.jobmate_connect.entity.User;
import com.quokka.jobmate_connect.exception.AppException;
import com.quokka.jobmate_connect.exception.ErrorCode;
import com.quokka.jobmate_connect.mapper.JobMapper;
import com.quokka.jobmate_connect.repository.ApplicationRepository;
import com.quokka.jobmate_connect.repository.JobRepository;
import com.quokka.jobmate_connect.repository.RatingRepository;
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
        ApplicationRepository applicationRepository;
        RatingRepository ratingRepository;

        static final double EARTH_RADIUS_KM = 6371.0;
        private final UserRepository userRepository;

        @Transactional
        public JobResponse createJob(JobCreationRequest request) {
                UUID userId = getUserId();

                User user = userRepository.findById(userId)
                                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

                double[] coordinates = geocodingService.getCoordinates(request.getLocation());

                Job job = Job.builder()
                                .title(request.getTitle())
                                .description(request.getDescription())
                                .requirements(request.getRequirements())
                                 .benefits(request.getBenefits())
                                .location(request.getLocation())
                                .latitude(coordinates[0])
                                .longitude(coordinates[1])
                                .salary(request.getSalary())
                                .salaryUnit(request.getSalaryUnit())
                                .jobType(request.getJobType())
                                .skills(request.getSkills())
                                .status(JobStatus.PENDING_REVIEW)
                                .createdBy(user)
                                .startAt(request.getStartAt())
                                .deadline(request.getDeadline())
                                .createdAt(LocalDateTime.now())

                                .companyName(request.getCompanyName())
                                .workingHours(request.getWorkingHours())
                                .workingDays(request.getWorkingDays())
                                .workMode(request.getWorkMode())
                                .category(request.getCategory())
                                .contactPhone(request.getContactPhone())
                                .applicationCount(0)
                                .viewsCount(0)
                                .build();

                jobRepository.save(job);

                notificationService.notifyAdmins("Yêu cầu duyệt công việc mới",
                                "Người dùng " + user.getFullName() +
                                                " đã tạo công việc mới: '" + job.getTitle() +
                                                "'. Vui lòng xem xét và phê duyệt.");

                notificationService.sendNotification(NotificationRequest.builder()
                                .userId(userId)
                                .title("Đăng công việc thành công")
                                .message("Công việc '" + job.getTitle() + "' đã được tạo và đang chờ phê duyệt.")
                                .type(NotificationType.SYSTEM)
                                .build());

                return mapToJobResponseWithStats(job);
        }

        public JobResponse getJobDetails(UUID jobId) {
                Job job = jobRepository.findById(jobId)
                                .orElseThrow(() -> new AppException(ErrorCode.JOB_NOT_FOUND));

                return jobMapper.toJobResponse(job);
        }

        public PageResponse<JobResponse> getAllJobs(int page, int size) {
                Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
                Page<Job> jobPage = jobRepository.findAll(pageable);

                List<JobResponse> responses = jobPage.getContent().stream()
                                .map(this::mapToJobResponseWithStats)
                                .toList();

                return PageResponse.<JobResponse>builder()
                                .currentPage(page + 1)
                                .totalPages(jobPage.getTotalPages())
                                .pageSize(size)
                                .totalElements(jobPage.getTotalElements())
                                .data(responses)
                                .build();
        }

        public PageResponse<JobResponse> getMyJobs(int page, int size, JobStatus status) {
                UUID userId = getUserId();

                Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
                Page<Job> jobPage = (status == null)
                                ? jobRepository.findByCreatedById(userId, pageable)
                                : jobRepository.findByCreatedByIdAndStatus(userId, status, pageable);

                List<JobResponse> responses = jobPage.getContent().stream()
                                .map(this::mapToJobResponseWithStats)
                                .toList();

                return PageResponse.<JobResponse>builder()
                                .currentPage(page)
                                .totalPages(jobPage.getTotalPages())
                                .pageSize(size)
                                .totalElements(jobPage.getTotalElements())
                                .data(responses)
                                .build();
        }

        public PageResponse<JobResponse> getNearByJob(double radiusKm, int page, int size) {
                UUID userId = getUserId();

                User user = userRepository.findById(userId)
                                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

                Pageable pageable = PageRequest.of(page, size);
                Page<Job> jobPage = jobRepository.findByStatus(JobStatus.APPROVED, pageable);

                List<JobResponse> nearby = jobPage.stream()
                                .map(job -> {
                                        double distance = geocodingService.calculateDistance(
                                                        user.getLatitude(), user.getLongitude(),
                                                        job.getLatitude(), job.getLongitude());

                                        JobResponse response = mapToJobResponseWithStats(job);
                                        response.setDistance(distance);
                                        return response;
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
                UUID userId = getUserId();

                Job job = jobRepository.findById(jobId)
                                .orElseThrow(() -> new RuntimeException("Job not found"));

                String message = null;

                if (status == JobStatus.APPROVED) {
                        job.setStatus(JobStatus.APPROVED);
                        message = message = "Công việc '" + job.getTitle()
                                        + "' của bạn đã được duyệt và hiển thị trên hệ thống.";
                } else if (status == JobStatus.REJECTED) {
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
                                .title(status == JobStatus.APPROVED ? "Công việc đã được duyệt"
                                                : "Công việc bị từ chối")
                                .message(message)
                                .build());
        }

        @Transactional
        public JobResponse updateJob(UUID jobId, JobCreationRequest request) {

                UUID userId = getUserId();

                User user = userRepository.findById(userId)
                                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

                Job job = jobRepository.findById(jobId)
                                .orElseThrow(() -> new AppException(ErrorCode.JOB_NOT_FOUND));

                if (!job.getCreatedBy().getId().equals(user.getId())) {
                        throw new AppException(ErrorCode.UNAUTHORIZED);
                }

                if (job.getStatus() != JobStatus.PENDING_REVIEW
                                && job.getStatus() != JobStatus.REJECTED
                                && job.getStatus() != JobStatus.CLOSED) {
                        throw new AppException(ErrorCode.JOB_CANNOT_BE_UPDATED);
                }

                job.setTitle(request.getTitle());
                job.setDescription(request.getDescription());
                job.setRequirements(request.getRequirements());
                job.setBenefits(request.getBenefits());
                job.setLocation(request.getLocation());
                job.setSalary(request.getSalary());
                job.setSalaryUnit(request.getSalaryUnit());
                job.setJobType(request.getJobType());
                job.setSkills(request.getSkills());
                job.setStartAt(request.getStartAt());
                job.setDeadline(request.getDeadline());

                job.setCompanyName(request.getCompanyName());
                job.setWorkingHours(request.getWorkingHours());
                job.setWorkingDays(request.getWorkingDays());
                job.setWorkMode(request.getWorkMode());
                job.setCategory(request.getCategory());
                job.setContactPhone(request.getContactPhone());

                double[] coordinates = geocodingService.getCoordinates(request.getLocation());
                job.setLatitude(coordinates[0]);
                job.setLongitude(coordinates[1]);

                job.setStatus(JobStatus.PENDING_REVIEW);

                job.setUpdatedAt(LocalDateTime.now());
                jobRepository.save(job);

                notificationService.notifyAdmins(
                                "Yêu cầu duyệt lại công việc",
                                "Người dùng " + user.getFullName() + " đã chỉnh sửa công việc '" + job.getTitle() +
                                                "' và gửi lại để duyệt.");

                return mapToJobResponseWithStats(job);
        }

        public PageResponse<JobResponse> getAvailableJobs(int page, int size, String keyword, String location) {
                Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
                Page<Job> jobPage;

                if (keyword != null && !keyword.isEmpty()) {
                        jobPage = jobRepository.findByTitleContainingIgnoreCaseAndStatus(keyword, JobStatus.APPROVED,
                                        pageable);
                } else if (location != null && !location.isEmpty()) {
                        jobPage = jobRepository.findByLocationContainingIgnoreCaseAndStatus(location,
                                        JobStatus.APPROVED, pageable);
                } else {
                        jobPage = jobRepository.findByStatus(JobStatus.APPROVED, pageable);
                }

                List<JobResponse> responses = jobPage.getContent().stream()
                                .map(this::mapToJobResponseWithStats)
                                .toList();

                return PageResponse.<JobResponse>builder()
                                .currentPage(page + 1)
                                .totalPages(jobPage.getTotalPages())
                                .pageSize(size)
                                .totalElements(jobPage.getTotalElements())
                                .data(responses)
                                .build();
        }

        public JobDetailResponse getJobDetailById(UUID jobId) {
                Job job = jobRepository.findById(jobId)
                                .orElseThrow(() -> new AppException(ErrorCode.JOB_NOT_FOUND));

                job.setViewsCount(job.getViewsCount() + 1);
                jobRepository.save(job);

                return jobMapper.toJobDetailResponse(job);
        }

        private JobResponse mapToJobResponseWithStats(Job job) {
                JobResponse response = jobMapper.toJobResponse(job);

                // Tính applicationCount
                Long appCount = applicationRepository.countByJobId(job.getId());
                response.setApplicationCount(appCount != null ? appCount.intValue() : 0);

                // Tính rating
                Double avgRating = ratingRepository.getAverageRatingByJobId(job.getId());
                Long ratingCount = ratingRepository.countByJobId(job.getId());
                response.setAverageRating(avgRating != null ? avgRating.floatValue() : null);
                response.setRatingCount(ratingCount != null ? ratingCount.intValue() : 0);

                return response;
        }

        private UUID getUserId() {
                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                Jwt jwt = (Jwt) auth.getPrincipal();
                Object claim = jwt.getClaim("userId");
                return UUID.fromString(String.valueOf(claim));
        }
}
