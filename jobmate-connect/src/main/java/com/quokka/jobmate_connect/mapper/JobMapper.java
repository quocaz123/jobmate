package com.quokka.jobmate_connect.mapper;

import com.quokka.jobmate_connect.dto.request.job.JobCreationRequest;
import com.quokka.jobmate_connect.dto.response.job.JobResponse;
import com.quokka.jobmate_connect.entity.Job;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface JobMapper {

    Job toJob(JobCreationRequest request);
    @Mapping(source = "createdBy.fullName", target = "createdByName")
    JobResponse toJobResponse(Job job);
}
