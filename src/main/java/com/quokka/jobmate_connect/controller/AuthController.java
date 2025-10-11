package com.quokka.jobmate_connect.controller;

import com.nimbusds.jose.JOSEException;
import com.quokka.jobmate_connect.dto.ApiResponse;
import com.quokka.jobmate_connect.dto.request.AuthenticationRequest;
import com.quokka.jobmate_connect.dto.request.IntrospectRequest;
import com.quokka.jobmate_connect.dto.request.LogoutRequest;
import com.quokka.jobmate_connect.dto.response.AuthenticationResponse;
import com.quokka.jobmate_connect.dto.response.IntrospectResponse;
import com.quokka.jobmate_connect.service.AuthenticationService;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.ParseException;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class AuthController {
    AuthenticationService authenticationService;

    @PostMapping("/login")
    ApiResponse<AuthenticationResponse> login(@RequestBody AuthenticationRequest request) {
        return ApiResponse.success(authenticationService.authenticate(request));
    }

    @PostMapping("/refresh")
    ApiResponse<AuthenticationResponse> refresh(@RequestBody IntrospectRequest request) {
        return ApiResponse.success(authenticationService.refresh(request));
    }

    @PostMapping("/logout")
    ApiResponse<Void> logout(@RequestBody LogoutRequest request)
            throws ParseException, JOSEException {
        authenticationService.logout(request);
        return ApiResponse.success(null);
    }
}
