package com.quokka.jobmate_connect.dto.response.job;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    
    Double score;
    Double salaryPerHour;
}
