package com.quokka.Notification_Service.kafka.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SendOtpEvent {
    String email;
    String otp;
    LocalDateTime timestamp;
}

