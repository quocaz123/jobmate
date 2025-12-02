package com.quokka.jobmate_connect.mapper;

import com.quokka.jobmate_connect.dto.response.waitinglist.WaitingListResponse;
import com.quokka.jobmate_connect.entity.WaitingList;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface WaitingListMapper {
    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "user.fullName", target = "fullName")
    WaitingListResponse toWaitingListResponse(WaitingList waitingList);
}
