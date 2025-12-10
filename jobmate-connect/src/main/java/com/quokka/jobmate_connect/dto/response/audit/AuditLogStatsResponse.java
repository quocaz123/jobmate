package com.quokka.jobmate_connect.dto.response.audit;

import lombok.Value;

@Value
public class AuditLogStatsResponse {
    String action;
    String actionLabel;
    long total;
}







