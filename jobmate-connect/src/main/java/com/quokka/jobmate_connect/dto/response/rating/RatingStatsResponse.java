package com.quokka.jobmate_connect.dto.response.rating;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RatingStatsResponse {
    Double averageRating;
    Long totalRatings;
    List<Map<String, Object>> ratingDistribution;
    String badgeLevel;
    Float trustScore;
}
