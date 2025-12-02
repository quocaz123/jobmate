package com.quokka.jobmate_connect.dto.response.dashboard;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class EmployerDashboardSummaryResponse {
    int totalJobs;
    int activeJobs;
    int pendingReviewJobs;
    int closedJobs;
    int totalApplications;
    int pendingApplications;
    int applicationsToday;
}

