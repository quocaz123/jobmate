package com.quokka.jobmate_connect.dto.response.dashboard;

import lombok.Builder;
import lombok.Singular;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class AdminSystemHealthResponse {
    String overallStatus;
    @Singular
    List<ServiceHealthDetailResponse> services;
}
