package com.quokka.jobmate_connect.mapper;

import com.quokka.jobmate_connect.dto.request.UserCreationRequest;
import com.quokka.jobmate_connect.dto.request.UserUpdateRequest;
import com.quokka.jobmate_connect.dto.response.UserResponse;
import com.quokka.jobmate_connect.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;


@Mapper(componentModel = "spring")
public interface UserMapper {

    User toUser(UserCreationRequest request);

    UserResponse toUserResponse(User user);
    @Mapping(target = "roles", ignore = true)
    void updateUser(@MappingTarget User user, UserUpdateRequest request);
}
