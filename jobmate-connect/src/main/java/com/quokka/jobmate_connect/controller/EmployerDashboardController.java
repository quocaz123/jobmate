package com.quokka.jobmate_connect.controller;

import com.quokka.jobmate_connect.dto.ApiResponse;
import com.quokka.jobmate_connect.dto.response.dashboard.EmployerDashboardSummaryResponse;
import com.quokka.jobmate_connect.dto.response.dashboard.EmployerRecentCandidateResponse;
import com.quokka.jobmate_connect.dto.response.dashboard.EmployerTopJobResponse;
import com.quokka.jobmate_connect.service.EmployerDashboardService;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/employer/dashboard")
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class EmployerDashboardController {

    EmployerDashboardService employerDashboardService;

    @PreAuthorize("hasRole('EMPLOYER')")
    @GetMapping("/summary")
    public ApiResponse<EmployerDashboardSummaryResponse> getSummary() {
        return ApiResponse.success(employerDashboardService.getSummary());
    }

    @PreAuthorize("hasRole('EMPLOYER')")
    @GetMapping("/jobs/top")
    public ApiResponse<List<EmployerTopJobResponse>> getTopJobs(
            @RequestParam(defaultValue = "5") int limit) {
        return ApiResponse.success(employerDashboardService.getTopJobs(limit));
    }

    @PreAuthorize("hasRole('EMPLOYER')")
    @GetMapping("/candidates/recent")
    public ApiResponse<List<EmployerRecentCandidateResponse>> getRecentCandidates(
            @RequestParam(defaultValue = "5") int limit) {
        return ApiResponse.success(employerDashboardService.getRecentCandidates(limit));
    }
}

