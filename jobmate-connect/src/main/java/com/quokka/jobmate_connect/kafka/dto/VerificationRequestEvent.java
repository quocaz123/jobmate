package com.quokka.jobmate_connect.kafka.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class VerificationRequestEvent {
    UUID userId;
    String email;
    String fullName;
    LocalDateTime requestedAt;
}
