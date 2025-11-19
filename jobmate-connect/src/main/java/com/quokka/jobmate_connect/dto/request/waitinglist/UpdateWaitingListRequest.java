package com.quokka.jobmate_connect.dto.request.waitinglist;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateWaitingListRequest {
    String jobType;
    String skills;
    BigDecimal expectedMinSalary;

    Integer searchRadius;
    String availableDays;
    String availableTime;

    String note;
}
