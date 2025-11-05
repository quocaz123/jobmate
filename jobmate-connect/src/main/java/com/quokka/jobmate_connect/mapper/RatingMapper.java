package com.quokka.jobmate_connect.mapper;

import com.quokka.jobmate_connect.dto.response.rating.RatingResponse;
import com.quokka.jobmate_connect.entity.Rating;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface RatingMapper {
    RatingResponse toRatingResponse(Rating rating);
}
