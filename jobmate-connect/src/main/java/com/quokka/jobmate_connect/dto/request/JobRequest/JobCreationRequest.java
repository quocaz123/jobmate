package com.quokka.jobmate_connect.dto.request.JobRequest;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobCreationRequest {
    String title;
    String description;
    BigDecimal salary;
    String location;
    String jobType;
    LocalDateTime startAt;
    LocalDateTime deadline;
    String skills;
}
