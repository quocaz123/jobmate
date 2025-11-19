package com.quokka.jobmate_connect.dto.response.job;

import com.quokka.jobmate_connect.constant.JobStatus;
import com.quokka.jobmate_connect.constant.JobType;
import com.quokka.jobmate_connect.constant.SalaryUnitType;
import com.quokka.jobmate_connect.dto.response.user.EmployerResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobDetailResponse {
    UUID id;
    String title;
    String description;
    String requirements;
    String benefits;
    SalaryUnitType salaryUnit;
    String location;
    BigDecimal salary;
    JobType jobType;
    String skills;
    String createdByName;
    LocalDateTime createdAt;
    LocalDateTime deadline;

    String companyName;
    Integer applicationCount;
    String workingHours;
    String workingDays;
    String workMode;
    String category;
    Integer viewsCount;
    String contactPhone;
    Float averageRating;
    Integer ratingCount;

    EmployerResponse employer;
}
