package com.quokka.jobmate_connect.dto.response.recommendation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobRecommendationResponse {
    UUID jobId;
    String title;
    String companyName;
    double semanticScore;
    double ruleScore;
    double finalScore;
    double distanceInKm;
}
