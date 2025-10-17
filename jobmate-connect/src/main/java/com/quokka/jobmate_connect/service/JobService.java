package com.quokka.jobmate_connect.service;

import com.quokka.jobmate_connect.dto.request.JobRequest.JobCreationRequest;
import com.quokka.jobmate_connect.dto.response.JobResponse.JobResponse;
import com.quokka.jobmate_connect.entity.Job;
import com.quokka.jobmate_connect.mapper.JobMapper;
import com.quokka.jobmate_connect.repository.JobRepository;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class JobService {
    JobRepository jobRepository;
    JobMapper jobMapper;

    public JobResponse createJob(JobCreationRequest request) {
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        Job job = jobMapper.toJob(request);
        return jobMapper.toJobResponse(jobRepository.save(job));
    }
}
