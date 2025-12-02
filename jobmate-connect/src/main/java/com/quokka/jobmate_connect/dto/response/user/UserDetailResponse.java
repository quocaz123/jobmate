package com.quokka.jobmate_connect.dto.response.user;


import com.quokka.jobmate_connect.constant.SalaryUnitType;
import com.quokka.jobmate_connect.constant.VerificationStatus;
import com.quokka.jobmate_connect.dto.response.file.FileResponse;
import com.quokka.jobmate_connect.dto.response.file.FileResumeResponse;
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
public class UserDetailResponse {
    UUID id;
    String email;
    String fullName;
    String address;
    String contactPhone;
    Double  latitude;
    Double  longitude;
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
    String preferredJobType;
    String availableDays;
    String availableTime;
    BigDecimal preferredMinSalary;
    SalaryUnitType preferredSalaryUnit;

    String bio;
    FileResumeResponse resume;
}
