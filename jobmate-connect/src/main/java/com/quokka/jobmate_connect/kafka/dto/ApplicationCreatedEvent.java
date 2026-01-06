package com.quokka.jobmate_connect.kafka.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ApplicationCreatedEvent {
    UUID applicationId;
    
    UUID candidateId;
    String candidateEmail;
    String candidateFullName;
    
    UUID employerId;
    String employerEmail;
    String employerFullName;
    
    UUID jobId;
    String jobTitle;
    
    String coverLetter; // Có thể null
    boolean hasResume;
    String resumeFileName; // Có thể null
    
    double matchScore; // Điểm khớp
    
    LocalDateTime appliedAt;
    LocalDateTime timestamp;
}









