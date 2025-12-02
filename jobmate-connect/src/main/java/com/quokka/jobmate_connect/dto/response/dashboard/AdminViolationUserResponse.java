package com.quokka.jobmate_connect.dto.response.dashboard;

import lombok.Builder;
import lombok.Value;

import java.util.UUID;

@Value
@Builder
public class AdminViolationUserResponse {
    UUID userId;
    String email;
    String fullName;
    Integer violationCount;
    String status;
}

