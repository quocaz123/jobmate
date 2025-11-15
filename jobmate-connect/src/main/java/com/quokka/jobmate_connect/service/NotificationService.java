package com.quokka.jobmate_connect.service;

import com.quokka.jobmate_connect.constant.NotificationType;
import com.quokka.jobmate_connect.dto.request.notification.NotificationRequest;
import com.quokka.jobmate_connect.dto.response.notification.NotificationResponse;
import com.quokka.jobmate_connect.entity.Notification;
import com.quokka.jobmate_connect.mapper.NotificationMapper;
import com.quokka.jobmate_connect.repository.NotificationRepository;
import com.quokka.jobmate_connect.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class NotificationService {
    NotificationRepository notificationRepository;
    NotificationMapper notificationMapper;
    UserRepository userRepository;

    public NotificationResponse sendNotification(NotificationRequest request) {
        Notification notification = Notification.builder()
                .userId(request.getUserId())
                .title(request.getTitle())
                .message(request.getMessage())
                .type(request.getType())
                .read(false)
                .createdAt(LocalDateTime.now())
                .build();

        notification = notificationRepository.save(notification);

        return notificationMapper.toNotificationResponse(notification);
    }

    public void notifyAdmins(String title, String message) {
        List<UUID> adminIds = userRepository.findAdminIds();

        for (UUID admin : adminIds) {
            sendNotification(NotificationRequest.builder()
                    .userId(admin)
                    .title(title)
                    .message(message)
                    .type(NotificationType.SYSTEM)
                    .build());
        }
    }

    public List<NotificationResponse> getNotificationsByUserId() {
        var jwt = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UUID userId = UUID.fromString(jwt.getClaim("userId").toString());
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(notificationMapper::toNotificationResponse)
                .toList();
    }

    public void markAsRead(UUID notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));
        notification.setRead(true);
        notificationRepository.save(notification);
    }

    @Transactional
    public void deleteAllMyNotifications() {
        var jwt = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UUID userId = UUID.fromString(jwt.getClaim("userId").toString());
        notificationRepository.deleteByUserId(userId);
    }

}
