package com.quokka.jobmate_connect.controller;

import com.quokka.jobmate_connect.constant.ApplicationStatus;
import com.quokka.jobmate_connect.dto.ApiResponse;
import com.quokka.jobmate_connect.dto.PageResponse;
import com.quokka.jobmate_connect.dto.request.application.ApplicationRequest;
import com.quokka.jobmate_connect.dto.response.application.ApplicationDetailResponse;
import com.quokka.jobmate_connect.dto.response.application.ApplicationListResponse;
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

    // ------------------------------------------------
    // ✅ Ứng viên nộp đơn
    // ------------------------------------------------
    @PostMapping("/apply")
    @PreAuthorize("hasRole('USER')")
    public ApiResponse<ApplicationResponse> applyJob(
            @RequestParam("jobId") UUID jobId,
            @RequestParam(required = false) String coverLetter,
            @RequestParam(required = false) MultipartFile resumeFile,
            @RequestParam(required = false, defaultValue = "false") boolean useProfileResume) {

        ApplicationRequest request = ApplicationRequest.builder()
                .jobId(jobId)
                .coverLetter(coverLetter)
                .resumeFile(resumeFile)
                .useProfileResume(useProfileResume)
                .build();

        return ApiResponse.success(applicationService.applyJob(request));
    }

    // ------------------------------------------------
    // ✅ Ứng viên xem chi tiết đơn ứng tuyển (hoặc nhà tuyển dụng xem ứng viên của mình)
    // ------------------------------------------------
    @GetMapping("/{id}")
    public ApiResponse<ApplicationDetailResponse> getApplicationDetail(@PathVariable UUID id) {
        return ApiResponse.success(applicationService.getApplicationDetail(id));
    }

    // ------------------------------------------------
    // ✅ Ứng viên xem danh sách các đơn ứng tuyển của chính mình
    // ------------------------------------------------
    @GetMapping("/my-applications")
    @PreAuthorize("hasRole('USER')")
    public ApiResponse<PageResponse<ApplicationListResponse>> getMyApplications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return ApiResponse.success(applicationService.getMyApplications(page, size));
    }

    // ------------------------------------------------
    // ✅ Nhà tuyển dụng xem danh sách ứng viên của một job cụ thể
    // ------------------------------------------------
    @GetMapping("/job/{jobId}")
    @PreAuthorize("hasRole('EMPLOYER') or hasRole('ADMIN')")
    public ApiResponse<PageResponse<ApplicationListResponse>> getJobApplications(
            @PathVariable("jobId") UUID jobId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) ApplicationStatus status) {

        return ApiResponse.success(applicationService.getJobApplications(page, size, jobId, status));
    }

    // ------------------------------------------------
    // ✅ Nhà tuyển dụng cập nhật trạng thái đơn ứng tuyển
    // ------------------------------------------------
    @PutMapping("/{applicationId}/status")
    @PreAuthorize("hasRole('EMPLOYER') or hasRole('ADMIN')")
    public ApiResponse<ApplicationResponse> updateApplicationStatus(
            @PathVariable("applicationId") UUID applicationId,
            @RequestParam("status") ApplicationStatus status,
            @RequestParam(required = false) String rejectionReason) {

        return ApiResponse.success(
                applicationService.updateApplicationStatus(applicationId, status, rejectionReason)
        );
    }

    // ------------------------------------------------
    // ✅ Ứng viên tự hủy đơn ứng tuyển của mình
    // ------------------------------------------------
    @PutMapping("/{applicationId}/cancel")
    public ApiResponse<Void> cancelApplication(@PathVariable UUID applicationId) {
        applicationService.cancelApplication(applicationId);
        return ApiResponse.success(null);
    }
}
