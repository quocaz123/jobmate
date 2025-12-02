package com.quokka.jobmate_connect.dto.response.dashboard;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;

@Value
@Builder
public class ServiceHealthDetailResponse {
    String name;
    String status;
    String message;
    LocalDateTime checkedAt;
}

