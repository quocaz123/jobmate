package com.quokka.jobmate_connect.dto.response.user;


import com.quokka.jobmate_connect.constant.VerificationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserListResponse {
    UUID id;
    String email;
    String fullName;
    String address;
    String contactPhone;
    String avatarUrl;
    Set<RoleResponse> roles;
    boolean isTwoFaEnabled;
    VerificationStatus verificationStatus;
    Float trustScore;
    String badgeLevel;
    Integer reviewCount;
    Integer violationCount;
    String status;
    LocalDateTime createdAt;

}
