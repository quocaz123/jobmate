package com.quokka.jobmate_connect.dto.response.dashboard;

import com.quokka.jobmate_connect.constant.ApplicationStatus;
import com.quokka.jobmate_connect.constant.JobStatus;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;
import java.util.UUID;

@Value
@Builder
public class EmployerRecentCandidateResponse {
    UUID applicationId;
    UUID candidateId;
    String candidateName;
    String candidateEmail;
    UUID jobId;
    String jobTitle;
    JobStatus jobStatus;
    ApplicationStatus status;
    LocalDateTime appliedAt;
    double matchScore;
    boolean resumeAvailable;
}
