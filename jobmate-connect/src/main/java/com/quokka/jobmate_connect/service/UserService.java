package com.quokka.jobmate_connect.service;

import com.quokka.jobmate_connect.constant.FileTypeStatus;
import com.quokka.jobmate_connect.constant.VerificationStatus;
import com.quokka.jobmate_connect.dto.PageResponse;
import com.quokka.jobmate_connect.dto.request.user.PasswordUpdateRequest;
import com.quokka.jobmate_connect.dto.request.user.TwoFaUpdateRequest;
import com.quokka.jobmate_connect.dto.request.user.UserCreationRequest;
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

        return userMapper.toUserResponse(userRepository.save(user));
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

        if (request.getAddress() != null && !request.getAddress().isEmpty()) {
            double[] coordinates = geocodingService.getCoordinates(request.getAddress());
            user.setLatitude(coordinates[0]);
            user.setLongitude(coordinates[1]);
            log.info("Geocoding address: {} to coordinates: {}, {}", request.getAddress(), coordinates[0],
                    coordinates[1]);
        }

        user.setUpdatedAt(LocalDateTime.now());

        User updatedUser = userRepository.save(user);
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

        return TwoFaStatusResponse.builder()
                .enabled(targetEnabled)
                .message(message)
                .build();
    }
}
