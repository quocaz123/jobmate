package com.quokka.jobmate_connect.controller;

import com.quokka.jobmate_connect.dto.ApiResponse;
import com.quokka.jobmate_connect.dto.response.dashboard.AdminDashboardSummaryResponse;
import com.quokka.jobmate_connect.dto.response.dashboard.AdminSystemHealthResponse;
import com.quokka.jobmate_connect.dto.response.dashboard.AdminViolationUserResponse;
import com.quokka.jobmate_connect.service.AdminDashboardService;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/admin/dashboard")
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class AdminDashboardController {

    AdminDashboardService adminDashboardService;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/summary")
    public ApiResponse<AdminDashboardSummaryResponse> getSummary() {
        return ApiResponse.success(adminDashboardService.getSummary());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/system-health")
    public ApiResponse<AdminSystemHealthResponse> getSystemHealth() {
        return ApiResponse.success(adminDashboardService.getSystemHealth());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/users/violations")
    public ApiResponse<List<AdminViolationUserResponse>> getTopViolationUsers(
            @RequestParam(defaultValue = "5") int limit) {
        return ApiResponse.success(adminDashboardService.getTopViolationEmployers(limit));
    }
}
