package com.quokka.jobmate_connect.dto.request.user;

import com.quokka.jobmate_connect.constant.SalaryUnitType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateRequest {
    String fullName;
    String contactPhone;
    String address;
    String avatarUrl;
    String skills;
    Double latitude;
    Double longitude;

    String preferredJobType;
    String availableDays;
    String availableTime;
    BigDecimal preferredMinSalary;
    SalaryUnitType preferredSalaryUnit;
    String bio;
}
