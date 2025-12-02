package com.quokka.jobmate_connect.mapper;

import com.quokka.jobmate_connect.dto.response.job.JobDetailResponse;
import com.quokka.jobmate_connect.dto.response.job.JobResponse;
import com.quokka.jobmate_connect.dto.response.user.EmployerResponse;
import com.quokka.jobmate_connect.entity.Job;
import com.quokka.jobmate_connect.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface JobMapper {

    @Mapping(source = "createdBy.fullName", target = "createdByName")
    @Mapping(source = "category.id", target = "categoryId")
    @Mapping(target = "distance", ignore = true)
    @Mapping(target = "averageRating", ignore = true)
    @Mapping(target = "ratingCount", ignore = true)
    JobResponse toJobResponse(Job job);

    @Mapping(source = "createdBy.fullName", target = "createdByName")
    @Mapping(source = "category.id", target = "categoryId")
    @Mapping(target = "averageRating", ignore = true)
    @Mapping(target = "ratingCount", ignore = true)
    @Mapping(target = "employer", expression = "java(toEmployerResponse(job.getCreatedBy()))")
    JobDetailResponse toJobDetailResponse(Job job);

    default EmployerResponse toEmployerResponse(User user) {
        if (user == null) {
            return null;
        }
        return EmployerResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .address(user.getAddress())
                .avatarUrl(user.getAvatarUrl())
                .badgeLevel(user.getBadgeLevel())
                .reviewCount(user.getReviewCount())
                .build();
    }
}
