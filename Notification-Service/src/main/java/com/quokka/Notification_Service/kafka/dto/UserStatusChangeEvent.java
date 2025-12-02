package com.quokka.Notification_Service.kafka.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserStatusChangeEvent {
    UUID userId;
    String email;
    String fullName;
    String status; // ACTIVE hoặc BANNED
    String reason;
    LocalDateTime processedAt;
}
