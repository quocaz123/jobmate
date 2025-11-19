package com.quokka.jobmate_connect.dto.response.job;

import com.quokka.jobmate_connect.constant.JobStatus;
import com.quokka.jobmate_connect.constant.JobType;
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
public class JobESResponse {
    String id;
    String title;
    String description;
    String jobType;
    Double salary;
    String salaryUnit;

    Double distance;

    String scheduleDays;
    String scheduleTime;
    String skills;

    String status;
}
