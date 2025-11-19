package com.quokka.jobmate_connect.dto.request.waitinglist;

import com.quokka.jobmate_connect.constant.JobType;
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
public class CreateWaitingListRequest {
    JobType jobType;
    String skills;
    BigDecimal expectedMinSalary;
    SalaryUnitType expectedSalaryUnit;
    Integer searchRadius;
    String availableDays;
    String availableTime;
    String note;
}
