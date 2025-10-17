package com.quokka.jobmate_connect.mapper;

import com.quokka.jobmate_connect.dto.request.JobRequest.JobCreationRequest;
import com.quokka.jobmate_connect.dto.response.JobResponse.JobResponse;
import com.quokka.jobmate_connect.entity.Job;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface JobMapper {
    Job toJob(JobCreationRequest request);

    JobResponse toJobResponse(Job job);
}
