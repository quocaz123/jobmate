package com.quokka.jobmate_connect.controller;

import com.quokka.jobmate_connect.dto.ApiResponse;
import com.quokka.jobmate_connect.dto.request.role.RoleUpgradeSubmissionRequest;
import com.quokka.jobmate_connect.dto.response.role.RoleUpgradeRequestResponse;
import com.quokka.jobmate_connect.service.RoleUpgradeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/role-upgrades")
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class RoleUpgradeController {

    RoleUpgradeService roleUpgradeService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Void> submitUpgradeRequest(@Valid @RequestBody RoleUpgradeSubmissionRequest request) {
        roleUpgradeService.submitRequest(request);
        return ApiResponse.success(null);
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<RoleUpgradeRequestResponse>> getMyRoleUpgradeRequests() {
        return ApiResponse.success(roleUpgradeService.getMyRequests());
    }
}



