package com.quokka.jobmate_connect.service;

import com.quokka.jobmate_connect.constant.ApplicationStatus;
import com.quokka.jobmate_connect.constant.AuditAction;
import com.quokka.jobmate_connect.constant.FileTypeStatus;
import com.quokka.jobmate_connect.constant.VerificationStatus;
import com.quokka.jobmate_connect.dto.PageResponse;
import com.quokka.jobmate_connect.constant.NotificationType;
import com.quokka.jobmate_connect.dto.request.notification.NotificationRequest;
import com.quokka.jobmate_connect.kafka.dto.UserStatusChangeEvent;
import com.quokka.jobmate_connect.kafka.topic.UserStatusEventProducer;
import com.quokka.jobmate_connect.dto.request.user.PasswordUpdateRequest;
import com.quokka.jobmate_connect.dto.request.user.TwoFaUpdateRequest;
import com.quokka.jobmate_connect.dto.request.user.UserCreationRequest;
import com.quokka.jobmate_connect.dto.request.user.UserStatusUpdateRequest;
import com.quokka.jobmate_connect.dto.request.user.UserUpdateRequest;
import com.quokka.jobmate_connect.dto.response.user.*;
import com.quokka.jobmate_connect.repository.ApplicationRepository;
import com.quokka.jobmate_connect.repository.JobRepository;
import com.quokka.jobmate_connect.repository.RatingRepository;
import com.quokka.jobmate_connect.constant.JobStatus;
import com.quokka.jobmate_connect.entity.Job;
import com.quokka.jobmate_connect.entity.Application;
import com.quokka.jobmate_connect.entity.Role;
import com.quokka.jobmate_connect.entity.User;
import com.quokka.jobmate_connect.exception.AppException;
import com.quokka.jobmate_connect.exception.ErrorCode;
import com.quokka.jobmate_connect.mapper.FileMapper;
import com.quokka.jobmate_connect.mapper.UserMapper;
import com.quokka.jobmate_connect.repository.FileMgtRepository;
import com.quokka.jobmate_connect.repository.RoleRepository;
import com.quokka.jobmate_connect.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import jakarta.transaction.Transactional;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class UserService {
        UserRepository userRepository;
        PasswordEncoder passwordEncoder;
        UserMapper userMapper;
        RoleRepository roleRepository;
        FileMgtRepository fileMgtRepository;
        GeocodingService geocodingService;
        FileMapper fileMapper;
        AuditLogService auditLogService;
        NotificationService notificationService;
        UserStatusEventProducer userStatusEventProducer;
        ApplicationRepository applicationRepository;
        JobRepository jobRepository;
        com.quokka.jobmate_connect.repository.ESRepository.JobESRepository jobESRepository;
        RatingRepository ratingRepository;

        public UserResponse createUser(UserCreationRequest request) {
                if (userRepository.existsByEmail(request.getEmail())) {
                        throw new AppException(ErrorCode.EMAIL_ALREADY_EXISTS);
                }
                User user = userMapper.toUser(request);
                user.setPassword(passwordEncoder.encode(request.getPassword()));

                HashSet<Role> roles = new HashSet<>();
                roleRepository.findByName("USER").ifPresent(roles::add);

                user.setRoles(roles);
                user.setVerificationStatus(VerificationStatus.UNVERIFIED);
                user.setStatus("ACTIVE");

                User savedUser = userRepository.save(user);
                auditLogService.record(savedUser, AuditAction.USER_CREATE_ACCOUNT, savedUser.getId(),
                                savedUser.getEmail(), "Đăng ký tài khoản");

                return userMapper.toUserResponse(savedUser);
        }

        public UserDetailResponse getMyInfo() {
                var auth = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
                UUID userId = UUID.fromString(auth.getClaim("userId"));

                User user = userRepository.findById(userId)
                                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

                UserDetailResponse response = new UserDetailResponse();
                response.setId(user.getId());
                response.setEmail(user.getEmail());
                response.setFullName(user.getFullName());
                response.setAvatarUrl(user.getAvatarUrl());
                response.setAddress(user.getAddress());
                response.setContactPhone(user.getContactPhone());
                response.setSkills(user.getSkills());
                response.setPreferredJobType(user.getPreferredJobType());
                response.setAvailableDays(user.getAvailableDays());
                response.setAvailableTime(user.getAvailableTime());
                response.setPreferredMinSalary(user.getPreferredMinSalary());
                response.setLatitude(user.getLatitude());
                response.setLongitude(user.getLongitude());
                response.setTrustScore(user.getTrustScore() != null ? user.getTrustScore() : 0f);
                response.setBadgeLevel(user.getBadgeLevel());
                response.setBio(user.getBio());
                response.setReviewCount(user.getReviewCount() != null ? user.getReviewCount() : 0);
                response.setViolationCount(user.getViolationCount() != null ? user.getViolationCount() : 0);
                response.setStatus(user.getStatus());
                response.setCreatedAt(user.getCreatedAt());
                response.setUpdatedAt(user.getUpdatedAt());
                response.setVerificationStatus(user.getVerificationStatus());
                response.setVerifiedAt(user.getVerifiedAt());
                response.setTwoFaEnabled(user.is_two_fa_enabled());

                // map roles (tránh null)
                Set<RoleResponse> roleResponses = user.getRoles() == null ? Set.of()
                                : user.getRoles().stream()
                                                .map(role -> RoleResponse.builder()
                                                                .name(role.getName())
                                                                .description(role.getDescription())
                                                                .build())
                                                .collect(Collectors.toSet());
                response.setRoles(roleResponses);

                fileMgtRepository.findByOwnerIdAndType(userId, FileTypeStatus.RESUME)
                                .map(fileMapper::toFileResumeResponse)
                                .ifPresent(response::setResume);

                return response;
        }

        public PageResponse<UserListResponse> getAllUsers(int page, int size, String status, String roleName) {
                Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

                Role role = null;
                if (roleName != null && !roleName.trim().isEmpty()) {
                        role = roleRepository.findByName(roleName)
                                        .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND));
                }

                Page<User> userPage = userRepository.findUserByStatus(status, pageable, role);

                List<UserListResponse> userResponses = userPage.getContent()
                                .stream()
                                .map(userMapper::toUserListResponse)
                                .toList();

                return PageResponse.<UserListResponse>builder()
                                .currentPage(userPage.getNumber())
                                .totalPages(userPage.getTotalPages())
                                .pageSize(userPage.getSize())
                                .totalElements(userPage.getTotalElements())
                                .data(userResponses)
                                .build();
        }

        public UserResponse getUserById(UUID id) {
                Optional<User> user = userRepository.findById(id);
                return user.map(userMapper::toUserResponse).orElse(null);
        }

        public UserResponse updateUser(UserUpdateRequest request) {
                String name = SecurityContextHolder.getContext().getAuthentication().getName();
                User user = userRepository.findByEmail(name)
                                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

                userMapper.updateUser(user, request);

                // Ưu tiên latitude/longitude trực tiếp từ request
                // Nếu không có, mới geocode từ address
                if (request.getLatitude() != null && request.getLongitude() != null) {
                        // User đã cung cấp tọa độ trực tiếp, không cần geocode
                        user.setLatitude(request.getLatitude());
                        user.setLongitude(request.getLongitude());
                        log.info("Set coordinates directly: {}, {}", request.getLatitude(), request.getLongitude());
                } else if (request.getAddress() != null && !request.getAddress().isEmpty()) {
                        // Geocode từ address nếu không có tọa độ trực tiếp
                        double[] coordinates = geocodingService.getCoordinates(request.getAddress());
                        if (coordinates == null) {
                                log.warn("Geocoding failed for address: {}", request.getAddress());
                                // Không throw exception ở đây vì đây là update profile, có thể bỏ qua location
                        } else {
                                user.setLatitude(coordinates[0]);
                                user.setLongitude(coordinates[1]);
                                log.info("Geocoding address: {} to coordinates: {}, {}", request.getAddress(),
                                                coordinates[0],
                                                coordinates[1]);
                        }
                }

                user.setUpdatedAt(LocalDateTime.now());
                User updatedUser = userRepository.save(user);
                auditLogService.record(updatedUser, AuditAction.USER_UPDATE_PROFILE, updatedUser.getId(),
                                updatedUser.getFullName(), "Cập nhật thông tin cá nhân");

                return userMapper.toUserResponse(updatedUser);
        }

        public PageResponse<UserResponse> getTopRatedUsers(int page, int size) {
                Pageable pageable = PageRequest.of(page, size);
                Page<User> users = userRepository.findByOrderByTrustScoreDesc(pageable);

                return PageResponse.<UserResponse>builder()
                                .currentPage(users.getNumber())
                                .pageSize(users.getSize())
                                .totalElements(users.getTotalElements())
                                .totalPages(users.getTotalPages())
                                .data(users.getContent()
                                                .stream()
                                                .map(userMapper::toUserResponse)
                                                .toList())
                                .build();
        }

        public List<UserResponse> getTop10RatedUsers() {
                List<User> users = userRepository.findTop10ByOrderByTrustScoreDesc();
                return users.stream()
                                .map(userMapper::toUserResponse)
                                .toList();
        }

        public UserStatsResponse getMyStats() {
                Jwt auth = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
                UUID userId = UUID.fromString(auth.getClaim("userId"));

                long totalApplications = applicationRepository.countByUser_Id(userId);
                long completedApplications = applicationRepository.countByUser_IdAndStatus(userId,
                                ApplicationStatus.ACCEPTED);

                long consideredApplications = applicationRepository.countByUser_IdAndStatusIn(
                                userId, List.of(ApplicationStatus.ACCEPTED, ApplicationStatus.REJECTED,
                                                ApplicationStatus.PENDING));

                double completionRate = 0.0;
                if (consideredApplications > 0) {
                        completionRate = (double) completedApplications / consideredApplications * 100.0;
                }

                Double averageRating = ratingRepository.getAverageRatingByUserId(userId);
                Long totalRatings = ratingRepository.countByToUserId(userId);

                return UserStatsResponse.builder()
                                .totalApplications((int) totalApplications)
                                .completedApplications((int) completedApplications)
                                .completionRate(completionRate)
                                .averageRating(averageRating != null ? averageRating : 0.0)
                                .totalRatings(totalRatings != null ? totalRatings : 0L)
                                .build();
        }

        public void updatePassword(PasswordUpdateRequest request) {
                Jwt userDetails = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
                User currentUser = userRepository.findByEmail(userDetails.getSubject())
                                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
                PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);

                if (!passwordEncoder.matches(request.getOldPassword(), currentUser.getPassword())) {
                        throw new AppException(ErrorCode.INVALID_OLD_PASSWORD);
                }

                if (!request.getNewPassword().equals(request.getConfirmPassword())) {
                        throw new AppException(ErrorCode.PASSWORD_MISMATCH);
                }

                if (request.getNewPassword().length() < 8) {
                        throw new AppException(ErrorCode.PASSWORD_TOO_SHORT);
                }

                currentUser.setPassword(passwordEncoder.encode(request.getNewPassword()));
                userRepository.save(currentUser);
                auditLogService.record(currentUser, AuditAction.USER_PASSWORD_CHANGE, currentUser.getId(),
                                currentUser.getEmail(), "Đổi mật khẩu thành công");
        }

        public TwoFaStatusResponse updateTwoFactorStatus(TwoFaUpdateRequest request) {
                Jwt auth = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
                UUID userId = UUID.fromString(auth.getClaim("userId"));

                User user = userRepository.findById(userId)
                                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

                boolean targetEnabled = Boolean.TRUE.equals(request.getEnabled());
                boolean currentEnabled = user.is_two_fa_enabled();

                if (currentEnabled == targetEnabled) {
                        String message = targetEnabled
                                        ? "Two-factor authentication is already enabled."
                                        : "Two-factor authentication is already disabled.";
                        return TwoFaStatusResponse.builder()
                                        .enabled(currentEnabled)
                                        .message(message)
                                        .build();
                }

                user.set_two_fa_enabled(targetEnabled);
                userRepository.save(user);

                String message = targetEnabled
                                ? "Two-factor authentication has been enabled successfully."
                                : "Two-factor authentication has been disabled successfully.";

                auditLogService.record(user,
                                targetEnabled ? AuditAction.USER_ENABLE_2FA : AuditAction.USER_DISABLE_2FA,
                                user.getId(),
                                user.getEmail(),
                                targetEnabled ? "Bật 2FA" : "Tắt 2FA");

                return TwoFaStatusResponse.builder()
                                .enabled(targetEnabled)
                                .message(message)
                                .build();
        }

        @Transactional
        public void upgradeUserToEmployer(UUID userId) {
                User user = userRepository.findById(userId)
                                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

                if (user.getVerificationStatus() != VerificationStatus.VERIFIED) {
                        throw new AppException(ErrorCode.USER_NOT_VERIFIED);
                }

                Set<Role> roles = user.getRoles();
                if (roles == null) {
                        roles = new HashSet<>();
                }

                boolean alreadyEmployer = roles.stream()
                                .anyMatch(role -> "EMPLOYER".equalsIgnoreCase(role.getName()));
                if (alreadyEmployer) {
                        throw new AppException(ErrorCode.ALREADY_EMPLOYER);
                }

                Role employerRole = roleRepository.findByName("EMPLOYER")
                                .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND));

                roles.add(employerRole);
                user.setRoles(roles);
                userRepository.save(user);
                UUID actorId = getCurrentUserIdOrNull();
                if (actorId != null && !actorId.equals(user.getId())) {
                        auditLogService.record(actorId, AuditAction.USER_PROMOTED_EMPLOYER, user.getId(),
                                        user.getEmail(), "Cấp quyền EMPLOYER");
                } else {
                        auditLogService.record(user, AuditAction.USER_PROMOTED_EMPLOYER, user.getId(),
                                        user.getEmail(), "Cấp quyền EMPLOYER");
                }
        }

        @Transactional
        public void updateUserStatus(UUID userId, UserStatusUpdateRequest request) {
                User user = userRepository.findById(userId)
                                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

                String oldStatus = user.getStatus();
                String newStatus = request.getStatus();

                if (newStatus.equalsIgnoreCase(oldStatus)) {
                        return;
                }
                // Cập nhật status
                user.setStatus(newStatus);
                userRepository.save(user);

                UUID adminId = getCurrentUserIdOrNull();
                String adminInfo = adminId != null ? adminId.toString() : "SYSTEM";

                String actionMessage = "ACTIVE".equalsIgnoreCase(newStatus)
                                ? "Mở khóa tài khoản"
                                : "Khóa tài khoản";
                String reason = request.getReason() != null && !request.getReason().isEmpty()
                                ? request.getReason()
                                : "Không có lý do";

                // Nếu user bị banned, đóng tất cả job đang hoạt động và thông báo cho ứng viên
                if ("BANNED".equalsIgnoreCase(newStatus)) {
                        handleUserBanned(user);
                }

                auditLogService.record(adminId != null ? userRepository.findById(adminId).orElse(null) : null,
                                AuditAction.USER_STATUS_CHANGE,
                                user.getId(),
                                user.getEmail(),
                                String.format("%s bởi admin %s. Lý do: %s", actionMessage, adminInfo, reason));

                String title = "ACTIVE".equalsIgnoreCase(newStatus)
                                ? "Tài khoản đã được mở khóa"
                                : "Tài khoản đã bị khóa";
                String message = "ACTIVE".equalsIgnoreCase(newStatus)
                                ? "Tài khoản của bạn đã được mở khóa. Bạn có thể sử dụng hệ thống bình thường."
                                : String.format("Tài khoản của bạn đã bị khóa. Lý do: %s", reason);

                notificationService.sendNotification(NotificationRequest.builder()
                                .userId(userId)
                                .title(title)
                                .message(message)
                                .type(NotificationType.SYSTEM)
                                .build());

                userStatusEventProducer.sendUserStatusChangeEvent(UserStatusChangeEvent.builder()
                                .userId(user.getId())
                                .email(user.getEmail())
                                .fullName(user.getFullName())
                                .status(newStatus)
                                .reason(reason)
                                .processedAt(LocalDateTime.now())
                                .build());
        }

        private void handleUserBanned(User user) {
                // 1. Xử lý khi user là employer: Đóng tất cả job đang hoạt động
                handleEmployerBanned(user);

                // 2. Xử lý khi user là ứng viên: Hủy tất cả application đang PENDING hoặc
                // ACCEPTED
                handleCandidateBanned(user);
        }

        private void handleEmployerBanned(User user) {
                // Tìm tất cả job đang hoạt động của user (APPROVED, PENDING_REVIEW)
                List<JobStatus> activeStatuses = Arrays.asList(JobStatus.APPROVED, JobStatus.PENDING_REVIEW);
                List<Job> activeJobs = jobRepository.findByCreatedByIdAndStatusIn(user.getId(), activeStatuses);

                if (!activeJobs.isEmpty()) {
                        log.info("Đóng {} job đang hoạt động của employer bị banned: {}", activeJobs.size(),
                                        user.getEmail());

                        for (Job job : activeJobs) {
                                // Đóng job
                                job.setStatus(JobStatus.CLOSED);
                                job.setUpdatedAt(LocalDateTime.now());
                                jobRepository.save(job);

                                // Xóa job khỏi Elasticsearch khi bị đóng
                                try {
                                        jobESRepository.deleteById(job.getId().toString());
                                } catch (Exception e) {
                                        log.warn("Không thể xóa job {} khỏi Elasticsearch: {}", job.getId(),
                                                        e.getMessage());
                                }

                                auditLogService.record((User) null, AuditAction.JOB_STATUS_CHANGE, job.getId(),
                                                job.getTitle(), "Tự động đóng do employer bị banned");

                                // Tìm tất cả application đang pending hoặc accepted của job này
                                List<Application> applications = applicationRepository.findByJobIdOrderByAppliedAtDesc(
                                                job.getId(), Pageable.unpaged()).getContent();

                                for (Application app : applications) {
                                        if (app.getStatus() == ApplicationStatus.PENDING ||
                                                        app.getStatus() == ApplicationStatus.ACCEPTED) {

                                                // Thông báo cho ứng viên
                                                notificationService.sendNotification(NotificationRequest.builder()
                                                                .userId(app.getUser().getId())
                                                                .title("Công việc đã bị đóng")
                                                                .message("Công việc '" + job.getTitle() +
                                                                                "' mà bạn đã ứng tuyển đã bị đóng do nhà tuyển dụng bị khóa tài khoản.")
                                                                .type(NotificationType.SYSTEM)
                                                                .build());

                                                log.info("Đã thông báo cho ứng viên {} về việc job {} bị đóng",
                                                                app.getUser().getEmail(), job.getTitle());
                                        }
                                }
                        }
                }
        }

        private void handleCandidateBanned(User user) {
                // Tìm tất cả application đang PENDING hoặc ACCEPTED của user
                List<ApplicationStatus> activeStatuses = Arrays.asList(ApplicationStatus.PENDING,
                                ApplicationStatus.ACCEPTED);
                List<Application> activeApplications = applicationRepository.findByUserIdOrderByAppliedAtDesc(
                                user.getId(), Pageable.unpaged()).getContent()
                                .stream()
                                .filter(app -> activeStatuses.contains(app.getStatus()))
                                .toList();

                if (!activeApplications.isEmpty()) {
                        log.info("Hủy {} application của ứng viên bị banned: {}", activeApplications.size(),
                                        user.getEmail());

                        for (Application app : activeApplications) {
                                Job job = app.getJob();
                                User employer = job.getCreatedBy();

                                // Đánh dấu application là CANCELLED
                                app.setStatus(ApplicationStatus.CANCELLED);
                                app.setCancelledAt(LocalDateTime.now());
                                app.setRejectionReason("Ứng viên đã bị khóa tài khoản");
                                applicationRepository.save(app);

                                // Cập nhật lại application count của job
                                updateJobApplicationCount(job);

                                // Thông báo cho employer
                                notificationService.sendNotification(NotificationRequest.builder()
                                                .userId(employer.getId())
                                                .title("Ứng viên đã bị khóa tài khoản")
                                                .message("Ứng viên '" + user.getFullName() +
                                                                "' đã bị khóa tài khoản. Đơn ứng tuyển cho công việc '"
                                                                +
                                                                job.getTitle() + "' đã bị hủy.")
                                                .type(NotificationType.SYSTEM)
                                                .build());

                                auditLogService.record((User) null, AuditAction.APPLICATION_CANCEL, app.getId(),
                                                "Application của " + user.getEmail(),
                                                "Tự động hủy do ứng viên bị banned");

                                log.info("Đã hủy application {} và thông báo cho employer {}",
                                                app.getId(), employer.getEmail());
                        }
                }
        }

        private void updateJobApplicationCount(Job job) {
                Long appCount = applicationRepository.countByJobId(job.getId());
                int totalApplications = appCount != null ? appCount.intValue() : 0;
                job.setApplicationCount(totalApplications);
                jobRepository.save(job);
        }

        private UUID getCurrentUserIdOrNull() {
                try {
                        var authentication = SecurityContextHolder.getContext().getAuthentication();
                        if (authentication == null) {
                                return null;
                        }
                        Object principal = authentication.getPrincipal();
                        if (principal instanceof Jwt jwt) {
                                Object claim = jwt.getClaim("userId");
                                return claim != null ? UUID.fromString(String.valueOf(claim)) : null;
                        }
                } catch (Exception ignored) {
                }
                return null;
        }
}
