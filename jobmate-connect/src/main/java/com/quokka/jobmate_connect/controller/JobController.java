package com.quokka.jobmate_connect.controller;

import com.quokka.jobmate_connect.constant.JobStatus;
import com.quokka.jobmate_connect.dto.ApiResponse;
import com.quokka.jobmate_connect.dto.PageResponse;
import com.quokka.jobmate_connect.dto.request.job.JobCreationRequest;
import com.quokka.jobmate_connect.dto.response.job.JobResponse;
import com.quokka.jobmate_connect.service.JobService;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

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

    @GetMapping("/my-jobs")
    public ApiResponse<PageResponse<JobResponse>> getMyPostedJobs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.success(jobService.getMyJobs(page, size));
    }

    @GetMapping("nearby")
    public ApiResponse<PageResponse<JobResponse>> getNearbyJobs(
            @RequestParam(defaultValue = "10") double radiusInKm,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size){

        var result = jobService.getNearByJob(radiusInKm, page, size);
        return ApiResponse.success(result);
    }

    @PreAuthorize("hasAnyRole('EMPLOYER', 'ADMIN')")
    @PutMapping("/{jobId}")
    public ApiResponse<JobResponse> updateJob(@PathVariable("jobId") UUID jobId, JobCreationRequest request) {
        return ApiResponse.success(jobService.updateJob(jobId, request));
    }

    @PutMapping("/{jobId}/verify-job")
    public ApiResponse<Void> verifyJob( @PathVariable UUID jobId,
                                        @RequestParam JobStatus status,
                                        @RequestParam(required = false) String reason) {
        jobService.updateJobVerificationStatus(jobId, status, reason);
        return ApiResponse.success(null);
    }

    @GetMapping("/available")
    public ApiResponse<PageResponse<JobResponse>> getAvailableJobs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String location) {
        return ApiResponse.success(jobService.getAvailableJobs(page, size, keyword, location));
    }

    @GetMapping("/{jobId} ")
    public ApiResponse<JobResponse> getJobDetail(@PathVariable UUID jobId) {
        return ApiResponse.success(jobService.getJobDetails(jobId));
    }
}
