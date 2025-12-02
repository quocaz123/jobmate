package com.quokka.jobmate_connect.controller;

import com.quokka.jobmate_connect.constant.ReportStatus;
import com.quokka.jobmate_connect.dto.ApiResponse;
import com.quokka.jobmate_connect.dto.PageResponse;
import com.quokka.jobmate_connect.dto.request.report.ReportRequest;
import com.quokka.jobmate_connect.dto.response.report.ReportResponse;
import com.quokka.jobmate_connect.service.ReportService;

import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class ReportController {

    ReportService reportService;

    // ============================================================
    // 1) USER GỬI REPORT
    // ============================================================
    @PostMapping
    public ApiResponse<ReportResponse> createReport(@RequestBody ReportRequest request) {
        return ApiResponse.success(reportService.createReport(request));
    }

    // ============================================================
    // 2) ADMIN XEM REPORT
    // ============================================================
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ApiResponse<PageResponse<ReportResponse>> getReports(
            @RequestParam(required = false) ReportStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ApiResponse.success(reportService.getReports(status, page, size));
    }

    // ============================================================
    // 3) ADMIN REVIEW REPORT
    // ============================================================
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/review")
    public ApiResponse<Void> reviewReport(
            @PathVariable UUID id,
            @RequestParam boolean accept,
            @RequestParam(required = false) String note
    ) {
        reportService.reviewReport(id, accept, note);
        return ApiResponse.success(null);
    }
}
