package com.quokka.jobmate_connect.service;

import com.quokka.jobmate_connect.constant.FileTypeStatus;
import com.quokka.jobmate_connect.constant.NotificationType;
import com.quokka.jobmate_connect.constant.VerificationStatus;
import com.quokka.jobmate_connect.dto.PageResponse;
import com.quokka.jobmate_connect.dto.request.notification.NotificationRequest;
import com.quokka.jobmate_connect.dto.response.verification.UserVerificationDetailResponse;
import com.quokka.jobmate_connect.dto.response.verification.UserVerificationListResponse;
import com.quokka.jobmate_connect.entity.Role;
import com.quokka.jobmate_connect.entity.User;
import com.quokka.jobmate_connect.exception.AppException;
import com.quokka.jobmate_connect.exception.ErrorCode;
import com.quokka.jobmate_connect.kafka.dto.VerificationRequestEvent;
import com.quokka.jobmate_connect.kafka.dto.VerificationResultEvent;
import com.quokka.jobmate_connect.kafka.topic.VerificationEventProducer;
import com.quokka.jobmate_connect.kafka.topic.VerificationResultProducer;
import com.quokka.jobmate_connect.repository.FileMgtRepository;
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
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserVerificationService {
    UserRepository userRepository;
    FileMgtRepository fileMgtRepository;
    S3Service s3Service;
    NotificationService notificationService;
    VerificationEventProducer verificationEventProducer;
    VerificationResultProducer verificationResultProducer;

    public void requestVerification() {
        var jwt = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UUID userId = UUID.fromString(jwt.getClaim("userId"));

        var user = userRepository.findById(userId).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        var hasFront = fileMgtRepository.findByOwnerIdAndType(userId, FileTypeStatus.CCCD_FRONT);
        var hasBack = fileMgtRepository.findByOwnerIdAndType(userId, FileTypeStatus.CCCD_BACK);
        var hasAvatar = fileMgtRepository.findByOwnerIdAndType(userId, FileTypeStatus.AVATAR);

        if (!hasFront.isPresent() || !hasBack.isPresent() || !hasAvatar.isPresent()) {
            throw new RuntimeException("Please upload both CCCD images and avatar before verifying.");
        }

        user.setVerificationStatus(VerificationStatus.PENDING);
        user.setVerificationRequestedAt(LocalDateTime.now());
        user.setRejectionReason(null);
        userRepository.save(user);

        notificationService.notifyAdmins("Có yêu cầu xác thực mới",
                "Người dùng " + user.getFullName() + " đã gửi yêu cầu xác thực tài khoản.");

        verificationEventProducer.sendVerificationRequestEvent(
                VerificationRequestEvent.builder()
                        .userId(userId)
                        .email(user.getEmail())
                        .fullName(user.getFullName())
                        .requestedAt(user.getVerificationRequestedAt())
                        .build());
    }

    public PageResponse<UserVerificationListResponse> getPendingUsers(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("verificationRequestedAt").descending());
        Page<User> pendingUsers = userRepository.findByVerificationStatus(VerificationStatus.PENDING, pageable);

        List<UserVerificationListResponse> userResponse = pendingUsers.getContent().stream()
                .map(user -> UserVerificationListResponse.builder()
                        .userId(user.getId())
                        .email(user.getEmail())
                        .fullName(user.getFullName())
                        .requestedAt(user.getVerificationRequestedAt())
                        .verificationStatus(user.getVerificationStatus().name())
                        .build())
                .toList();

        return PageResponse.<UserVerificationListResponse>builder()
                .currentPage(pendingUsers.getNumber())
                .totalPages(pendingUsers.getTotalPages())
                .pageSize(size)
                .totalElements(pendingUsers.getTotalElements())
                .data(userResponse)
                .build();
    }

    public UserVerificationDetailResponse getVerificationDetail(UUID userId) {
        var user = userRepository.findById(userId).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        var frontFile = fileMgtRepository.findByOwnerIdAndType(user.getId(), FileTypeStatus.CCCD_FRONT).orElse(null);
        var backFile = fileMgtRepository.findByOwnerIdAndType(user.getId(), FileTypeStatus.CCCD_BACK).orElse(null);
        var avatarFile = fileMgtRepository.findByOwnerIdAndType(user.getId(), FileTypeStatus.CCCD_BACK).orElse(null);

        return UserVerificationDetailResponse.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .avatarUrl(avatarFile != null ? s3Service.generatePresignedUrl(avatarFile.getS3Key(), 10) : null)
                .cccdFrontUrl(frontFile != null ? s3Service.generatePresignedUrl(frontFile.getS3Key(), 10) : null)
                .cccdBackUrl(backFile != null ? s3Service.generatePresignedUrl(backFile.getS3Key(), 10) : null)
                .requestedAt(user.getVerificationRequestedAt())
                .build();
    }

    @Transactional
    public void approveVerification(UUID userId) {

        var user = userRepository.findById(userId).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        user.setVerificationStatus(VerificationStatus.VERIFIED);
        user.setVerifiedAt(LocalDateTime.now());
        user.setRejectionReason(null);
        userRepository.save(user);

        var cccdFiles = fileMgtRepository.findAllByOwnerId(userId).stream()
                .filter(f -> f.getType() == FileTypeStatus.CCCD_FRONT ||
                        f.getType() == FileTypeStatus.CCCD_BACK)
                .toList();

        for (var file : cccdFiles) {
            if (file.getS3Key() != null) {
                s3Service.deleteFile(file.getS3Key());
            }
            fileMgtRepository.delete(file);
        }

        notificationService.sendNotification(NotificationRequest.builder()
                .userId(userId)
                .title("Xác thực tài khoản thành công")
                .message("Tài khoản của bạn đã được xác thực thành công. Chào mừng bạn đến với cộng đồng JobMate Connect!")
                .type(NotificationType.SYSTEM)
                .build());

        verificationResultProducer.sendVerificationResultEvent(
                VerificationResultEvent.builder()
                        .userId(userId)
                        .email(user.getEmail())
                        .fullName(user.getFullName())
                        .isApproved(true)
                        .processedAt(LocalDateTime.now())
                        .build());
    }

    @Transactional
    public void rejectVerification(UUID userId, String reason) {
        var user = userRepository.findById(userId).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        user.setVerificationStatus(VerificationStatus.REJECTED);
        user.setRejectionReason(reason);
        userRepository.save(user);

        notificationService.sendNotification(NotificationRequest.builder()
                .userId(userId)
                .title("Xác thực tài khoản thất bại")
                .message("Yêu cầu xác thực tài khoản của bạn đã bị từ chối. Lý do: " + reason)
                .type(NotificationType.SYSTEM)
                .build());

        verificationResultProducer.sendVerificationResultEvent(
                VerificationResultEvent.builder()
                        .userId(userId)
                        .email(user.getEmail())
                        .fullName(user.getFullName())
                        .isApproved(false)
                        .reason(reason)
                        .processedAt(LocalDateTime.now())
                        .build());
    }
}
