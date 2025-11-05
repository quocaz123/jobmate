package com.quokka.jobmate_connect.controller;

import com.quokka.jobmate_connect.constant.ApplicationStatus;
import com.quokka.jobmate_connect.dto.ApiResponse;
import com.quokka.jobmate_connect.dto.PageResponse;
import com.quokka.jobmate_connect.dto.request.application.ApplicationRequest;
import com.quokka.jobmate_connect.dto.response.application.ApplicationResponse;
import com.quokka.jobmate_connect.service.ApplicationService;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/applications")
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class ApplicationController {
    ApplicationService applicationService;

    @PostMapping("/apply")
    public ApiResponse<ApplicationResponse> applyJob(
            @RequestParam("jobId") UUID jobId,
            @RequestParam(value = "coverLetter") String coverLetter,
            @RequestParam(value = "resumeFile", required = false)MultipartFile resumeFile) {

        ApplicationRequest request = ApplicationRequest.builder()
                .jobId(jobId)
                .coverLetter(coverLetter)
                .resumeFile(resumeFile)
                .build();
        return ApiResponse.success(applicationService.applyJob(request));
    }

    @GetMapping("/my-applications")
    public ApiResponse<PageResponse<ApplicationResponse>> getMyApplication(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.success(applicationService.getMyApplications(page, size));
    }

    @PreAuthorize("hasRole('EMPLOYER') or hasRole('ADMIN')")
    @GetMapping("/job/{jobId}")
    public ApiResponse<PageResponse<ApplicationResponse>> getJobApplications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @PathVariable("jobId") UUID jobId) {
        return ApiResponse.success(applicationService.getJobApplications(page, size, jobId));
    }

    @PreAuthorize("hasRole('EMPLOYER') or hasRole('ADMIN')")
    @PutMapping("/{applicationId}/status")
    public ApiResponse<ApplicationResponse> updateApplicationStatus(
            @PathVariable("applicationId") UUID applicationId,
            @RequestParam("status") ApplicationStatus status,
            @RequestParam(required = false) String rejectionReason) {
        return ApiResponse.success(applicationService.updateApplicationStatus(applicationId, status, rejectionReason));
    }

    @PutMapping("/{applicationId}/cancel")
    public ApiResponse<Void> cancelApplication(
            @PathVariable UUID applicationId) {
        applicationService.cancelApplication(applicationId);
        return ApiResponse.success(null);
    }
}
