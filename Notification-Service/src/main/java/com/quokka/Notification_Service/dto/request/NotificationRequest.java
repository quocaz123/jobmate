package com.quokka.Notification_Service.dto.request;

import com.quokka.Notification_Service.constant.NotificationType;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class NotificationRequest {
    UUID userId;
    String title;
    String message;
    NotificationType type;
}
