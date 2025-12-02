package com.quokka.jobmate_connect.controller;

import com.quokka.jobmate_connect.constant.VerificationStatus;
import com.quokka.jobmate_connect.dto.ApiResponse;
import com.quokka.jobmate_connect.dto.PageResponse;
import com.quokka.jobmate_connect.dto.request.user.UserStatusUpdateRequest;
import com.quokka.jobmate_connect.dto.response.verification.UserVerificationDetailResponse;
import com.quokka.jobmate_connect.dto.response.verification.UserVerificationListResponse;
import com.quokka.jobmate_connect.service.UserService;
import com.quokka.jobmate_connect.service.UserVerificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/admin/verify")
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class AdminVerifyController {
    UserVerificationService userVerificationService;
    UserService userService;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/pending")
    public ApiResponse<PageResponse<UserVerificationListResponse>> getPendingUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "PENDING", required = false) VerificationStatus status) {
        var result = userVerificationService.getPendingUsers(page, size, status);
        return ApiResponse.success(result);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/pending/{userId}/detail")
    public ApiResponse<UserVerificationDetailResponse> getVerificationDetail(
            @PathVariable UUID userId) {
        return ApiResponse.success(userVerificationService.getVerificationDetail(userId));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{userId}/approve")
    public ApiResponse<String> approve(@PathVariable UUID userId) {
        userVerificationService.approveVerification(userId);
        return ApiResponse.success("User verification approved.");
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{userId}/reject")
    public ApiResponse<String> reject(@PathVariable UUID userId,
            @RequestParam String reason) {
        userVerificationService.rejectVerification(userId, reason);
        return ApiResponse.success("User verification rejected: " + reason);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{userId}/status")
    public ApiResponse<Void> updateUserStatus(
            @PathVariable UUID userId,
            @Valid @RequestBody UserStatusUpdateRequest request) {
        userService.updateUserStatus(userId, request);
        return ApiResponse.success(null);
    }

}
