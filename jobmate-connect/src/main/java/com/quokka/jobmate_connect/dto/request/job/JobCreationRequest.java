package com.quokka.jobmate_connect.dto.request.job;

import com.quokka.jobmate_connect.constant.JobType;
import com.quokka.jobmate_connect.constant.SalaryUnitType;
import com.quokka.jobmate_connect.entity.User;
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
public class JobCreationRequest {
    String title;
    String description;
    String requirements;
    String benefits;
    String location;
    Double latitude;
    Double longitude;
    BigDecimal salary;
    JobType jobType;
    SalaryUnitType salaryUnit;
    LocalDateTime deadline;
    String skills;
    User createdBy;
    String companyName;
    String workingHours;
    String workingDays;
    String workMode;
    UUID categoryId;
    Integer targetApplicants;
    String contactPhone;
}
