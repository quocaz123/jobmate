package com.quokka.jobmate_connect.controller;

import com.quokka.jobmate_connect.constant.RoleUpgradeStatus;
import com.quokka.jobmate_connect.dto.ApiResponse;
import com.quokka.jobmate_connect.dto.PageResponse;
import com.quokka.jobmate_connect.dto.request.role.RoleUpgradeDecisionRequest;
import com.quokka.jobmate_connect.dto.request.role.RoleUpgradeRejectionRequest;
import com.quokka.jobmate_connect.dto.response.role.RoleUpgradeRequestResponse;
import com.quokka.jobmate_connect.service.RoleUpgradeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/admin/role-upgrades")
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class AdminRoleUpgradeController {

    RoleUpgradeService roleUpgradeService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<PageResponse<RoleUpgradeRequestResponse>> getRoleUpgradeRequests(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) RoleUpgradeStatus status) {

        return ApiResponse.success(roleUpgradeService.getRequests(status, page, size));
    }

    @PostMapping("/{requestId}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> approveRoleUpgrade(
            @PathVariable UUID requestId,
            @RequestBody(required = false) @Valid RoleUpgradeDecisionRequest request) {

        roleUpgradeService.approveRequest(requestId, request != null ? request : new RoleUpgradeDecisionRequest());
        return ApiResponse.success(null);
    }

    @PostMapping("/{requestId}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> rejectRoleUpgrade(
            @PathVariable UUID requestId,
            @RequestBody @Valid RoleUpgradeRejectionRequest request) {

        roleUpgradeService.rejectRequest(requestId, request);
        return ApiResponse.success(null);
    }
}



