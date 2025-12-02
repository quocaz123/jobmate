package com.quokka.jobmate_connect.dto.response.dashboard;

import com.quokka.jobmate_connect.constant.JobStatus;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;
import java.util.UUID;

@Value
@Builder
public class EmployerTopJobResponse {
    UUID jobId;
    String title;
    int totalApplications;
    int targetApplicants;
    int viewsCount;
    JobStatus status;
    LocalDateTime lastApplicationAt;
    boolean targetReached;
}
