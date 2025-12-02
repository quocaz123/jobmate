package com.quokka.jobmate_connect.mapper;

import com.quokka.jobmate_connect.dto.request.user.UserCreationRequest;
import com.quokka.jobmate_connect.dto.request.user.UserUpdateRequest;
import com.quokka.jobmate_connect.dto.response.user.UserListResponse;
import com.quokka.jobmate_connect.dto.response.user.UserResponse;
import com.quokka.jobmate_connect.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;


@Mapper(componentModel = "spring")
public interface UserMapper {

    User toUser(UserCreationRequest request);

    UserResponse toUserResponse(User user);

    UserListResponse toUserListResponse(User user);
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "email", ignore = true)
    @Mapping(target = "latitude", ignore = true)
    @Mapping(target = "longitude", ignore = true)
    void updateUser(@MappingTarget User user, UserUpdateRequest request);
}
