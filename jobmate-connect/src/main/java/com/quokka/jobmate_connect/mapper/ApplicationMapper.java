package com.quokka.jobmate_connect.mapper;

import com.quokka.jobmate_connect.dto.request.application.ApplicationRequest;
import com.quokka.jobmate_connect.dto.response.application.ApplicationResponse;
import com.quokka.jobmate_connect.entity.Application;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ApplicationMapper {

    Application toApplication(ApplicationRequest request);

    @Mapping(source = "job.id", target = "jobId")
    @Mapping(source = "user.id", target = "userId")
    ApplicationResponse toApplicationResponse(Application application);
}
