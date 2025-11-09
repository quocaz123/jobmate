package com.quokka.jobmate_connect.dto.response.application;

import com.quokka.jobmate_connect.constant.ApplicationStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ApplicationDetailResponse {
    UUID id;
    ApplicationStatus status;
    String coverLetter;
    boolean hasResume;
    String resumeFileName;
    LocalDateTime appliedAt;
    LocalDateTime cancelledAt;
    String rejectionReason;
    Double matchScore;

    // Applicant info
    UUID applicantId;
    String applicantName;
    String email;
    String contactPhone;
    String address;
    String skills;
    String preferredJobType;
    String bio;
    String avatarUrl;

    // Job info
    UUID jobId;
    String jobTitle;
    String companyName;
    BigDecimal salary;
    String salaryUnit;
    String workingDays;
    String workingHours;
}
