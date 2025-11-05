package com.quokka.jobmate_connect.dto.response.user;


import com.quokka.jobmate_connect.constant.VerificationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    UUID id;
    String email;
    String fullName;
    String address;
    String avatarUrl;
    String skills;
    Set<RoleResponse> roles;
    boolean isTwoFaEnabled;
    VerificationStatus verificationStatus;
    LocalDateTime verifiedAt;
    Float trustScore;
    String badgeLevel;
    Integer reviewCount;
    Integer violationCount;
    String status;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
