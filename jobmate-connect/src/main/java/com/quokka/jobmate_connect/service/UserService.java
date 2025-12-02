package com.quokka.jobmate_connect.service;

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

        User user = userRepository.findById(userId).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

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

        // Tìm Role nếu roleName được cung cấp
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
        User user = userRepository.findByEmail(name).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

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
            user.setLatitude(coordinates[0]);
            user.setLongitude(coordinates[1]);
            log.info("Geocoding address: {} to coordinates: {}, {}", request.getAddress(), coordinates[0],
                    coordinates[1]);
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

        // Kiểm tra nếu status không thay đổi
        if (newStatus.equalsIgnoreCase(oldStatus)) {
            log.info("User {} status is already {}", userId, newStatus);
            return;
        }

        // Cập nhật status
        user.setStatus(newStatus);
        userRepository.save(user);

        // Lấy admin hiện tại
        UUID adminId = getCurrentUserIdOrNull();
        String adminInfo = adminId != null ? adminId.toString() : "SYSTEM";

        // Ghi audit log
        String actionMessage = "ACTIVE".equalsIgnoreCase(newStatus)
                ? "Mở khóa tài khoản"
                : "Khóa tài khoản";
        String reason = request.getReason() != null && !request.getReason().isEmpty()
                ? request.getReason()
                : "Không có lý do";

        auditLogService.record(adminId != null ? userRepository.findById(adminId).orElse(null) : null,
                AuditAction.USER_STATUS_CHANGE,
                user.getId(),
                user.getEmail(),
                String.format("%s bởi admin %s. Lý do: %s", actionMessage, adminInfo, reason));

        // Gửi thông báo cho user
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

        // Gửi email thông báo qua Kafka
        userStatusEventProducer.sendUserStatusChangeEvent(UserStatusChangeEvent.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .status(newStatus)
                .reason(reason)
                .processedAt(LocalDateTime.now())
                .build());

        log.info("Admin {} updated user {} status from {} to {}", adminInfo, userId, oldStatus, newStatus);
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
