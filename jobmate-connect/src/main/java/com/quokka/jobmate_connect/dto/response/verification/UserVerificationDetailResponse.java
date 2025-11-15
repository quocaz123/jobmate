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
public class UserVerificationDetailResponse {
    UUID userId;
    String email;
    String fullName;
    String address;
    String contactPhone;
    String avatarUrl;
    String cccdFrontUrl;
    String cccdBackUrl;
    String rejectionReason;
    LocalDateTime requestedAt;
}
