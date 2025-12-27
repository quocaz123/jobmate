package com.quokka.jobmate_connect.service;

import com.quokka.jobmate_connect.constant.AuditAction;
import com.quokka.jobmate_connect.constant.JobStatus;
import com.quokka.jobmate_connect.constant.JobType;
import com.quokka.jobmate_connect.constant.NotificationType;
import com.quokka.jobmate_connect.dto.PageResponse;
import com.quokka.jobmate_connect.dto.request.job.JobCreationRequest;
import com.quokka.jobmate_connect.dto.request.notification.NotificationRequest;
import com.quokka.jobmate_connect.dto.response.job.JobDetailResponse;
import com.quokka.jobmate_connect.dto.response.job.JobResponse;
import com.quokka.jobmate_connect.entity.Category;
import com.quokka.jobmate_connect.entity.Job;
import com.quokka.jobmate_connect.entity.User;
import com.quokka.jobmate_connect.exception.AppException;
import com.quokka.jobmate_connect.exception.ErrorCode;
import com.quokka.jobmate_connect.mapper.JobMapper;
import com.quokka.jobmate_connect.repository.*;
import com.quokka.jobmate_connect.service.ESService.JobIndexerService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class JobService {
        JobRepository jobRepository;
        JobMapper jobMapper;
        GeocodingService geocodingService;
        NotificationService notificationService;
        JobInvitationService jobInvitationService;
        ApplicationRepository applicationRepository;
        RatingRepository ratingRepository;
        JobIndexerService indexer;
        CategoryRepository categoryRepository;
        AuditLogService auditLogService;
        UserRepository userRepository;
        com.quokka.jobmate_connect.repository.ESRepository.JobESRepository jobESRepository;

        static final double EARTH_RADIUS_KM = 6371.0;

        @Transactional
        public JobResponse createJob(JobCreationRequest request) {
                UUID userId = getUserId();

                User user = userRepository.findById(userId)
                                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

                // Kiểm tra user có bị banned không
                if ("BANNED".equalsIgnoreCase(user.getStatus())) {
                        throw new AppException(ErrorCode.USER_BANNED);
                }

                // Validate và xử lý location và tọa độ
                Double latitude = request.getLatitude();
                Double longitude = request.getLongitude();

                // Nếu có tọa độ từ request, validate chúng
                if (latitude != null && longitude != null) {
                        if (!isValidCoordinate(latitude, longitude)) {
                                throw new AppException(ErrorCode.INVALID_COORDINATES);
                        }
                } else {
                        // Nếu không có tọa độ, cần location để geocode
                        if (request.getLocation() == null || request.getLocation().trim().isEmpty()) {
                                throw new AppException(ErrorCode.LOCATION_REQUIRED);
                        }

                        double[] coordinates = geocodingService.getCoordinates(request.getLocation());
                        if (coordinates == null) {
                                throw new AppException(ErrorCode.GEOCODING_FAILED);
                        }
                        latitude = coordinates[0];
                        longitude = coordinates[1];
                }

                Integer targetApplicants = request.getTargetApplicants() != null ? request.getTargetApplicants() : 1;
                Category category = resolveCategory(request.getCategoryId());

                Job job = Job.builder()
                                .title(request.getTitle())
                                .description(request.getDescription())
                                .requirements(request.getRequirements())
                                .benefits(request.getBenefits())
                                .location(request.getLocation())
                                .latitude(latitude)
                                .longitude(longitude)
                                .salary(request.getSalary())
                                .salaryUnit(request.getSalaryUnit())
                                .jobType(request.getJobType())
                                .skills(request.getSkills())
                                .status(JobStatus.PENDING_REVIEW)
                                .createdBy(user)
                                .deadline(request.getDeadline())
                                .createdAt(LocalDateTime.now())

                                .companyName(request.getCompanyName())
                                .workingHours(request.getWorkingHours())
                                .workingDays(request.getWorkingDays())
                                .workMode(request.getWorkMode())
                                .category(category)
                                .categoryName(category != null ? category.getName() : null)
                                .contactPhone(request.getContactPhone())
                                .applicationCount(0)
                                .viewsCount(0)
                                .targetApplicants(targetApplicants)
                                .build();

                jobRepository.save(job);
                auditLogService.record(user, AuditAction.JOB_CREATE, job.getId(),
                                job.getTitle(), "Khởi tạo công việc mới");

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
                Page<Job> jobPage = jobRepository.findAllJobsByStatus(JobStatus.PENDING_REVIEW, pageable);

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

                // Lấy tất cả jobs (không paginate trong query để có thể sort theo application mới nhất)
                List<Job> allJobs;
                if (status == null) {
                        allJobs = jobRepository.findByCreatedByIdAndStatusNot(userId, JobStatus.DELETED, 
                                PageRequest.of(0, Integer.MAX_VALUE, Sort.by("createdAt").descending())).getContent();
                } else if (status == JobStatus.DELETED) {
                        allJobs = List.of();
                } else {
                        allJobs = jobRepository.findByCreatedByIdAndStatus(userId, status, 
                                PageRequest.of(0, Integer.MAX_VALUE, Sort.by("createdAt").descending())).getContent();
                }

                if (allJobs.isEmpty()) {
                        return PageResponse.<JobResponse>builder()
                                        .currentPage(page)
                                        .totalPages(0)
                                        .pageSize(size)
                                        .totalElements(0)
                                        .data(List.of())
                                        .build();
                }

                // Lấy thời gian application mới nhất cho tất cả jobs
                List<UUID> jobIds = allJobs.stream().map(Job::getId).toList();
                List<Object[]> latestApplications = applicationRepository.findLatestAppliedAtByJobIds(jobIds);
                
                // Tạo map: jobId -> thời gian application mới nhất
                Map<UUID, LocalDateTime> latestAppliedAtMap = new HashMap<>();
                for (Object[] result : latestApplications) {
                        UUID jobId = (UUID) result[0];
                        LocalDateTime latestAppliedAt = (LocalDateTime) result[1];
                        latestAppliedAtMap.put(jobId, latestAppliedAt);
                }

                // Sắp xếp: jobs có application mới nhất lên đầu, sau đó jobs không có application (theo createdAt)
                final Map<UUID, LocalDateTime> finalMap = latestAppliedAtMap;
                List<Job> sortedJobs = allJobs.stream()
                                .sorted((a, b) -> {
                                        LocalDateTime aLatest = finalMap.get(a.getId());
                                        LocalDateTime bLatest = finalMap.get(b.getId());
                                        
                                        // Nếu cả 2 đều có application mới nhất, sort theo thời gian application (mới nhất trước)
                                        if (aLatest != null && bLatest != null) {
                                                return bLatest.compareTo(aLatest);
                                        }
                                        // Nếu chỉ a có application, a lên đầu
                                        if (aLatest != null) {
                                                return -1;
                                        }
                                        // Nếu chỉ b có application, b lên đầu
                                        if (bLatest != null) {
                                                return 1;
                                        }
                                        // Cả 2 đều không có application, sort theo createdAt (mới nhất trước)
                                        return b.getCreatedAt().compareTo(a.getCreatedAt());
                                })
                                .toList();

                // Paginate thủ công
                int start = page * size;
                int end = Math.min(start + size, sortedJobs.size());
                List<Job> pagedJobs = start < sortedJobs.size() 
                        ? sortedJobs.subList(start, end) 
                        : List.of();

                List<JobResponse> responses = pagedJobs.stream()
                                .map(this::mapToJobResponseWithStats)
                                .toList();

                int totalPages = (int) Math.ceil((double) sortedJobs.size() / size);

                return PageResponse.<JobResponse>builder()
                                .currentPage(page)
                                .totalPages(totalPages)
                                .pageSize(size)
                                .totalElements(sortedJobs.size())
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
                        message = "Công việc '" + job.getTitle()
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

                indexer.index(job);

                notificationService.sendNotification(NotificationRequest.builder()
                                .userId(job.getCreatedBy().getId())
                                .title(status == JobStatus.APPROVED ? "Công việc đã được duyệt"
                                                : "Công việc bị từ chối")
                                .message(message)
                                .build());

                auditLogService.record(userId, AuditAction.JOB_STATUS_CHANGE, job.getId(),
                                job.getTitle(), "Trạng thái mới: " + status.name());
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
                job.setDeadline(request.getDeadline());

                Category category = resolveCategory(request.getCategoryId());

                job.setCompanyName(request.getCompanyName());
                job.setWorkingHours(request.getWorkingHours());
                job.setWorkingDays(request.getWorkingDays());
                job.setWorkMode(request.getWorkMode());
                job.setCategory(category);
                job.setCategoryName(category != null ? category.getName() : null);
                if (request.getTargetApplicants() != null) {
                        job.setTargetApplicants(request.getTargetApplicants());
                }
                job.setContactPhone(request.getContactPhone());

                Double latitude = request.getLatitude();
                Double longitude = request.getLongitude();
                if (latitude == null || longitude == null) {
                        if (request.getLocation() == null || request.getLocation().trim().isEmpty()) {
                                throw new AppException(ErrorCode.LOCATION_REQUIRED);
                        }
                        double[] coordinates = geocodingService.getCoordinates(request.getLocation());
                        if (coordinates == null) {
                                throw new AppException(ErrorCode.GEOCODING_FAILED);
                        }
                        latitude = coordinates[0];
                        longitude = coordinates[1];
                }

                if (!isValidCoordinate(latitude, longitude)) {
                        throw new AppException(ErrorCode.INVALID_COORDINATES);
                }

                job.setLatitude(latitude);
                job.setLongitude(longitude);

                job.setStatus(JobStatus.PENDING_REVIEW);

                job.setUpdatedAt(LocalDateTime.now());
                jobRepository.save(job);

                notificationService.notifyAdmins(
                                "Yêu cầu duyệt lại công việc",
                                "Người dùng " + user.getFullName() + " đã chỉnh sửa công việc '" + job.getTitle() +
                                                "' và gửi lại để duyệt.");

                auditLogService.record(user, AuditAction.JOB_UPDATE, job.getId(),
                                job.getTitle(), "Gửi lại để duyệt");

                return mapToJobResponseWithStats(job);
        }

        public PageResponse<JobResponse> getAvailableJobs(int page,
                        int size,
                        String keyword,
                        String location,
                        JobType jobType,
                        String workMode,
                        UUID categoryId,
                        BigDecimal salaryMin,
                        BigDecimal salaryMax) {
                // Lấy user hiện tại để tính khoảng cách (nếu có tọa độ)
                UUID currentUserId = null;
                User currentUser = null;
                try {
                        currentUserId = getUserId();
                        currentUser = userRepository.findById(currentUserId).orElse(null);
                } catch (Exception ignored) {
                        // Trường hợp không có user (public call) thì bỏ qua tính distance
                }

                final User finalCurrentUser = currentUser;

                Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
                Page<Job> jobPage = jobRepository.searchAvailableJobs(
                                JobStatus.APPROVED,
                                keyword,
                                location,
                                jobType,
                                (workMode != null && !workMode.isBlank()) ? workMode : null,
                                categoryId,
                                salaryMin,
                                salaryMax,
                                pageable);

                List<JobResponse> responses = jobPage.getContent().stream()
                                .map(job -> {
                                        JobResponse response = mapToJobResponseWithStats(job);

                                        // Tính distance nếu có đủ tọa độ user + job
                                        if (finalCurrentUser != null &&
                                                        finalCurrentUser.getLatitude() != null &&
                                                        finalCurrentUser.getLongitude() != null &&
                                                        job.getLatitude() != null &&
                                                        job.getLongitude() != null) {

                                                double distance = geocodingService.calculateDistance(
                                                                finalCurrentUser.getLatitude(),
                                                                finalCurrentUser.getLongitude(),
                                                                job.getLatitude(), job.getLongitude());
                                                response.setDistance(distance);
                                        }
                                        return response;
                                })
                                .toList();

                return PageResponse.<JobResponse>builder()
                                .currentPage(page + 1)
                                .totalPages(jobPage.getTotalPages())
                                .pageSize(size)
                                .totalElements(jobPage.getTotalElements())
                                .data(responses)
                                .build();
        }

        public Void closeJob(UUID jobId) {

                UUID userId = getUserId();

                Job job = jobRepository.findById(jobId)
                                .orElseThrow(() -> new AppException(ErrorCode.JOB_NOT_FOUND));

                if (!job.getCreatedBy().getId().equals(userId)) {
                        throw new AppException(ErrorCode.UNAUTHORIZED);
                }

                if (job.getStatus() != JobStatus.APPROVED &&
                                job.getStatus() != JobStatus.PENDING_REVIEW &&
                                job.getStatus() != JobStatus.REJECTED) {
                        throw new AppException(ErrorCode.JOB_INVALID_STATUS_CLOSE);
                }

                job.setStatus(JobStatus.CLOSED);
                job.setUpdatedAt(LocalDateTime.now());
                jobRepository.save(job);

                // Xóa job khỏi Elasticsearch khi bị đóng
                try {
                        jobESRepository.deleteById(job.getId().toString());
                } catch (Exception e) {
                        log.warn("Không thể xóa job {} khỏi Elasticsearch: {}", job.getId(), e.getMessage());
                }

                // Hủy/expire các lời mời còn pending cho job
                jobInvitationService.expirePendingInvitationsForJob(job);
                auditLogService.record(userId, AuditAction.JOB_CLOSE, job.getId(),
                                job.getTitle(), "Người đăng tự đóng job");
                return null;
        }

        public Void deleteJob(UUID jobId) {
                UUID userId = getUserId();

                Job job = jobRepository.findById(jobId)
                                .orElseThrow(() -> new AppException(ErrorCode.JOB_NOT_FOUND));

                if (!job.getCreatedBy().getId().equals(userId)) {
                        throw new AppException(ErrorCode.UNAUTHORIZED);
                }

                if (job.getStatus() != JobStatus.PENDING_REVIEW &&
                                job.getStatus() != JobStatus.REJECTED && job.getStatus() != JobStatus.CLOSED
                                && job.getStatus() != JobStatus.AUTO_CLOSED) {
                        throw new AppException(ErrorCode.JOB_INVALID_STATUS_DELETE);
                }

                job.setStatus(JobStatus.DELETED);
                job.setUpdatedAt(LocalDateTime.now());
                jobRepository.save(job);

                // Xóa job khỏi Elasticsearch khi bị xóa
                try {
                        jobESRepository.deleteById(job.getId().toString());
                } catch (Exception e) {
                        log.warn("Không thể xóa job {} khỏi Elasticsearch: {}", job.getId(), e.getMessage());
                }

                auditLogService.record(userId, AuditAction.JOB_DELETE, job.getId(),
                                job.getTitle(), "Đánh dấu job đã xóa");
                return null;
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

                // Tính rating theo nhà tuyển dụng (chủ job)
                if (job.getCreatedBy() != null) {
                        UUID ownerId = job.getCreatedBy().getId();
                        Double avgRating = ratingRepository.getAverageRatingByUserId(ownerId);
                        Long ratingCount = ratingRepository.countByToUserId(ownerId);
                        response.setAverageRating(avgRating != null ? avgRating.floatValue() : null);
                        response.setRatingCount(ratingCount != null ? ratingCount.intValue() : 0);
                } else {
                        response.setAverageRating(null);
                        response.setRatingCount(0);
                }

                return response;
        }

        private Category resolveCategory(UUID categoryId) {
                if (categoryId == null) {
                        return null;
                }
                return categoryRepository.findById(categoryId)
                                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));
        }

        private UUID getUserId() {
                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                Jwt jwt = (Jwt) auth.getPrincipal();
                Object claim = jwt.getClaim("userId");
                return UUID.fromString(String.valueOf(claim));
        }

        private boolean isValidCoordinate(double latitude, double longitude) {
                return latitude >= -90 && latitude <= 90 && longitude >= -180 && longitude <= 180;
        }
}
