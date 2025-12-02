package com.quokka.jobmate_connect.dto.response.audit;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;
import java.util.UUID;

@Value
@Builder
public class AuditLogResponse {
    UUID id;
    UUID userId;
    String userEmail;
    String userFullName;
    String action;
    UUID targetId;
    String description;
    LocalDateTime createdAt;
}

