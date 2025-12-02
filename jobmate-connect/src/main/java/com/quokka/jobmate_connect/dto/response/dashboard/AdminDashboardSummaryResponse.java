package com.quokka.jobmate_connect.dto.response.dashboard;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class AdminDashboardSummaryResponse {
    int totalUsers;
    int totalEmployers;
    int totalJobs;
    int totalReports;
    int newUsersToday;
    int newUsersThisWeek;
    int pendingJobs;
    int pendingReports;
}
