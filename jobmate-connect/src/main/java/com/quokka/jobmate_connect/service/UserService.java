package com.quokka.jobmate_connect.service;

import com.quokka.jobmate_connect.constant.VerificationStatus;
import com.quokka.jobmate_connect.dto.PageResponse;
import com.quokka.jobmate_connect.dto.request.user.PasswordUpdateRequest;
import com.quokka.jobmate_connect.dto.request.user.UserCreationRequest;
import com.quokka.jobmate_connect.dto.request.user.UserUpdateRequest;
import com.quokka.jobmate_connect.dto.response.user.UserResponse;
import com.quokka.jobmate_connect.entity.Role;
import com.quokka.jobmate_connect.entity.User;
import com.quokka.jobmate_connect.exception.AppException;
import com.quokka.jobmate_connect.exception.ErrorCode;
import com.quokka.jobmate_connect.mapper.UserMapper;
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
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class UserService {
    UserRepository userRepository;
    PasswordEncoder passwordEncoder;
    UserMapper userMapper;
    RoleRepository roleRepository;
    FileService fileService;
    GeocodingService geocodingService;

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

    public UserResponse getMyInfo() {
        String name = SecurityContextHolder.getContext().getAuthentication().getName();

        User user = userRepository.findByEmail(name).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        return userMapper.toUserResponse(user);
    }

    public PageResponse<UserResponse> getAllUsers(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<User> userPage = userRepository.findAll(pageable);

        List<UserResponse> userResponses = userPage.getContent()
                .stream()
                .map(userMapper::toUserResponse)
                .toList();

        return PageResponse.<UserResponse>builder()
                .currentPage(userPage.getNumber())
                .totalElements(userPage.getTotalElements())
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
            log.info("Geocoding address: {} to coordinates: {}, {}", request.getAddress(), coordinates[0], coordinates[1]);
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
}
