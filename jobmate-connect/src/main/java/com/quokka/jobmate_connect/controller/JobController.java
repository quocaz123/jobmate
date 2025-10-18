package com.quokka.jobmate_connect.controller;

import com.quokka.jobmate_connect.dto.ApiResponse;
import com.quokka.jobmate_connect.dto.PageResponse;
import com.quokka.jobmate_connect.dto.request.job.JobCreationRequest;
import com.quokka.jobmate_connect.dto.response.job.JobResponse;
import com.quokka.jobmate_connect.service.JobService;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/jobs")
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class JobController {
    JobService jobService;

    @PreAuthorize("hasRole('EMPLOYER', 'ADMIN')")
    @PostMapping()
    public ApiResponse<JobResponse> createJob(@RequestBody JobCreationRequest request) {
        return ApiResponse.success(jobService.createJob(request));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping()
    public ApiResponse<PageResponse<JobResponse>> getAllJobs(
            @RequestParam int page,
            @RequestParam int size) {
        return ApiResponse.success(jobService.getAllJobs(page, size));
    }

    @GetMapping("nearby")
    public ApiResponse<PageResponse<JobResponse>> getNearbyJobs(
            @RequestParam(defaultValue = "10") double radiusInKm,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size){

        var result = jobService.getNearByJob(radiusInKm, page, size);
        return ApiResponse.success(result);
    }


}
