package com.quokka.jobmate_connect.controller;

import com.quokka.jobmate_connect.dto.ApiResponse;
import com.quokka.jobmate_connect.dto.PageResponse;
import com.quokka.jobmate_connect.dto.request.user.UserCreationRequest;
import com.quokka.jobmate_connect.dto.response.user.UserResponse;
import com.quokka.jobmate_connect.service.UserService;
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

    @PostMapping("/registration")
    ApiResponse<UserResponse> createUser(@RequestBody UserCreationRequest request) {
        return ApiResponse.success(userService.createUser(request));
    }

    @GetMapping("/my-info")
    ApiResponse<UserResponse> getMyInfo(){
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

    @GetMapping("/{id}")
    ApiResponse<UserResponse> getUserById(@PathVariable UUID id) {
        return ApiResponse.success(userService.getUserById(id));
    }
}
