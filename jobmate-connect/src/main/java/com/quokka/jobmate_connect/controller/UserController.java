package com.quokka.jobmate_connect.controller;

import com.quokka.jobmate_connect.dto.ApiResponse;
import com.quokka.jobmate_connect.dto.PageResponse;
import com.quokka.jobmate_connect.dto.request.user.LocationRequest;
import com.quokka.jobmate_connect.dto.request.user.UserCreationRequest;
import com.quokka.jobmate_connect.dto.request.user.UserUpdateRequest;
import com.quokka.jobmate_connect.dto.response.user.LocationResponse;
import com.quokka.jobmate_connect.dto.response.user.UserDetailResponse;
import com.quokka.jobmate_connect.dto.response.user.UserResponse;
import com.quokka.jobmate_connect.service.LocationService;
import com.quokka.jobmate_connect.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
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

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping()
    ApiResponse<PageResponse<UserResponse>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        var results = userService.getAllUsers(page, size);
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

    @GetMapping("/location/auto")
    public ApiResponse<LocationResponse> getUserAutoLocation(HttpServletRequest req) {
        return ApiResponse.success(locationService.getAutoLocation(req));
    }
}
