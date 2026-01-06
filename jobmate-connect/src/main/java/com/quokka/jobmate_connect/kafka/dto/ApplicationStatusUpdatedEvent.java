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
public class ApplicationStatusUpdatedEvent {
    UUID applicationId;
    String status; // "ACCEPTED", "REJECTED", "CANCELLED"
    
    UUID candidateId;
    String candidateEmail;
    String candidateFullName;
    
    UUID employerId;
    String employerEmail;
    String employerFullName;
    
    UUID jobId;
    String jobTitle;
    
    String reason; // Lý do từ chối (nếu có)
    
    LocalDateTime updatedAt;
    LocalDateTime timestamp;
}









