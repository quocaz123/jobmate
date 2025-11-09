package com.quokka.jobmate_connect.dto.response.application;

import com.quokka.jobmate_connect.constant.ApplicationStatus;
import com.quokka.jobmate_connect.constant.JobStatus;
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
public class ApplicationListResponse {
     UUID applicationId;
     ApplicationStatus status;
     LocalDateTime appliedAt;
     Double matchScore;

     // Applicant summary
     UUID applicantId;
     String fullName;
     String avatarUrl;
     String address;
     String skills;
     String preferredJobType;
     Float trustScore;


     UUID jobId;
     String jobTitle;
     String companyName;
     String location;
     BigDecimal salary;
     String salaryUnit;
     String workingDays;
     String workingHours;
     String jobType;
     String statusJob;
}
