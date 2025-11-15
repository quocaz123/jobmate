package com.quokka.jobmate_connect.dto.response.notification;

import com.quokka.jobmate_connect.constant.NotificationType;
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
    Boolean read;
    String createdAt;
}