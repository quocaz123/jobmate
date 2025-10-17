package com.quokka.Notification_Service.dto.response;

import com.quokka.Notification_Service.constant.NotificationType;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class NotificationResponse {
    UUID id;
    UUID userId;
    String title;
    String message;
    NotificationType type;
    boolean isRead;
    String createdAt;
}
