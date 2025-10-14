package com.quokka.jobmate_connect.dto.response.JobResponse;

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
    String name;
    String title;
    String description;
    BigDecimal salary;
    String location;
    String jobType;
    LocalDateTime startAt;
    LocalDateTime deadline;
    String skills;
    JobStatus status;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
