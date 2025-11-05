package com.quokka.jobmate_connect.dto.response.application;

import com.quokka.jobmate_connect.constant.ApplicationStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ApplicationResponse {
    UUID id;
    UUID jobId;
    String jobTitle;
    UUID userId;
    String userName;
    String userEmail;
    ApplicationStatus status;
    String coverLetter;
    String resumeUrl;
    LocalDateTime appliedAt;
    LocalDateTime cancelledAt;
    String rejectionReason;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
