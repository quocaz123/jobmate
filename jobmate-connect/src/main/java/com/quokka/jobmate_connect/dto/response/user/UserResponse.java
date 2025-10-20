package com.quokka.jobmate_connect.dto.response.user;


import com.quokka.jobmate_connect.constant.VerificationStatus;
import com.quokka.jobmate_connect.dto.response.user.RoleResponse;
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
    Set<RoleResponse> roles;
    boolean isTwoFaEnabled;
    VerificationStatus verificationStatus;
    LocalDateTime verifiedAt;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
