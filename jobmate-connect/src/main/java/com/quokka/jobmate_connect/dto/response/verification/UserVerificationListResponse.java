package com.quokka.jobmate_connect.dto.response.verification;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserVerificationListResponse {
    UUID userId;
    String avatarUrl;
    String email;
    String fullName;
    LocalDateTime requestedAt;
    String verificationStatus;
}
