package com.quokka.jobmate_connect.dto.response.job;

import com.quokka.jobmate_connect.constant.JobStatus;
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
public class JobResponse {
    UUID id;
    String title;
    String description;
    String location;
    Double latitude;
    Double longitude;
    BigDecimal salary;
    String jobType;
    String skills;
    JobStatus status;
    String createdByName;
    LocalDateTime createdAt;
    LocalDateTime deadline;
    Double distance;
    String rejectionReason;
}
