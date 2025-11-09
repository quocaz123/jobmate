package com.quokka.jobmate_connect.mapper;

import com.quokka.jobmate_connect.dto.response.rating.RatingResponse;
import com.quokka.jobmate_connect.entity.Rating;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RatingMapper {
    @Mapping(source = "fromUser.id", target = "fromUserId")
    @Mapping(source = "fromUser.fullName", target = "fromUserName")
    @Mapping(source = "fromUser.avatarUrl", target = "fromUserAvatar")
    @Mapping(source = "toUser.id", target = "toUserId")
    @Mapping(source = "toUser.fullName", target = "toUserName")
    @Mapping(source = "job.id", target = "jobId")
    @Mapping(source = "job.title", target = "jobTitle")
    RatingResponse toRatingResponse(Rating rating);
}
