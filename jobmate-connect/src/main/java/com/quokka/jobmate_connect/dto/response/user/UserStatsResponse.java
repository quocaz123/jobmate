package com.quokka.jobmate_connect.dto.response.user;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class UserStatsResponse {
    int totalApplications;
    int completedApplications;
    double completionRate;

    Double averageRating;
    Long totalRatings;
}

