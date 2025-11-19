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
public class CandidateRecommendationResponse {
    UUID userId;
    String fullName;
    String skills;
    double semanticScore;
    double ruleScore;
    double finalScore;
    double distanceInKm;
}
