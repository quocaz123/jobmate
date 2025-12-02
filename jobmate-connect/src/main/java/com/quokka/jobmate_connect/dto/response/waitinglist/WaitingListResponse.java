package com.quokka.jobmate_connect.dto.response.waitinglist;

import com.quokka.jobmate_connect.constant.JobType;
import com.quokka.jobmate_connect.constant.RequestStatus;
import com.quokka.jobmate_connect.constant.SalaryUnitType;
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
public class WaitingListResponse {
    UUID id;
    UUID userId;
    String fullName;

    JobType jobType;
    String skills;
    BigDecimal expectedMinSalary;
    SalaryUnitType expectedSalaryUnit;

    Double latitude;
    Double longitude;
    Integer searchRadius;

    String availableDays;
    String availableTime;

    String note;

    RequestStatus status;

    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
