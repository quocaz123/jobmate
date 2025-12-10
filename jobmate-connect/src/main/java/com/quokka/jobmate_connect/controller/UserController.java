package com.quokka.jobmate_connect.controller;

import com.quokka.jobmate_connect.dto.ApiResponse;
import com.quokka.jobmate_connect.dto.PageResponse;
import com.quokka.jobmate_connect.dto.request.user.LocationRequest;
import com.quokka.jobmate_connect.dto.request.user.PasswordUpdateRequest;
import com.quokka.jobmate_connect.dto.request.user.TwoFaUpdateRequest;
import com.quokka.jobmate_connect.dto.request.user.UserCreationRequest;
import com.quokka.jobmate_connect.dto.request.user.UserUpdateRequest;
import com.quokka.jobmate_connect.dto.response.user.*;
import com.quokka.jobmate_connect.service.LocationService;
import com.quokka.jobmate_connect.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class UserController {
    UserService userService;
    LocationService locationService;

    @PostMapping("/registration")
    ApiResponse<UserResponse> createUser(@RequestBody UserCreationRequest request) {
        return ApiResponse.success(userService.createUser(request));
    }

    @GetMapping("/my-info")
    ApiResponse<UserDetailResponse> getMyInfo() {
        return ApiResponse.success(userService.getMyInfo());
    }

    @GetMapping("/my-stats")
    ApiResponse<UserStatsResponse> getMyStats() {
        return ApiResponse.success(userService.getMyStats());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping()
    ApiResponse<PageResponse<UserListResponse>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String role) {

        var results = userService.getAllUsers(page, size, status, role);
        return ApiResponse.success(results);
    }

    @PutMapping()
    public ApiResponse<UserResponse> updateUser(
            @RequestBody UserUpdateRequest request) {
        return ApiResponse.success(userService.updateUser(request));
    }

    @GetMapping("/{id}")
    ApiResponse<UserResponse> getUserById(@PathVariable UUID id) {
        return ApiResponse.success(userService.getUserById(id));
    }

    @GetMapping("/top-rated")
    public ApiResponse<PageResponse<UserResponse>> getTopRatedUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.success(userService.getTopRatedUsers(page, size));
    }

    @GetMapping("/top-10")
    public ApiResponse<List<UserResponse>> getTop10RatedUsers() {
        return ApiResponse.success(userService.getTop10RatedUsers());
    }

    @PutMapping("/location")
    public ApiResponse<Void> updateLocation(@RequestBody LocationRequest request) {
        locationService.updateLocation(request);
        return ApiResponse.success(null);
    }

    @PutMapping("/two-fa")
    public ApiResponse<TwoFaStatusResponse> updateTwoFa(@Valid @RequestBody TwoFaUpdateRequest request) {
        return ApiResponse.success(userService.updateTwoFactorStatus(request));
    }

    @PutMapping("/password")
    public ApiResponse<Void> updatePassword(@Valid @RequestBody PasswordUpdateRequest request) {
        userService.updatePassword(request);
        return ApiResponse.success(null);
    }

    @GetMapping("/location/auto")
    public ApiResponse<LocationResponse> getUserAutoLocation(HttpServletRequest req) {
        return ApiResponse.success(locationService.getAutoLocation(req));
    }

    @PatchMapping("/{id}/upgrade-employer")
    public ApiResponse<Void> upgradeUserToEmployer(@PathVariable UUID id) {
        userService.upgradeUserToEmployer(id);
        return ApiResponse.success(null);
    }
}
