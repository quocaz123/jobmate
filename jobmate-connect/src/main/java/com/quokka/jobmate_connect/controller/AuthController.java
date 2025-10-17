package com.quokka.jobmate_connect.controller;

import com.nimbusds.jose.JOSEException;
import com.quokka.jobmate_connect.dto.ApiResponse;
import com.quokka.jobmate_connect.dto.request.AuthenticationRequest;
import com.quokka.jobmate_connect.dto.request.IntrospectRequest;
import com.quokka.jobmate_connect.dto.request.LogoutRequest;
import com.quokka.jobmate_connect.dto.request.VerifyOtpRequest;
import com.quokka.jobmate_connect.dto.response.AuthenticationResponse;
import com.quokka.jobmate_connect.dto.response.IntrospectResponse;
import com.quokka.jobmate_connect.service.AuthenticationService;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class AuthController {
    AuthenticationService authenticationService;

    @PostMapping("/outbound/authentication")
    ApiResponse<AuthenticationResponse> outboundAuthentication(@RequestParam("code") String code) {
        var result = authenticationService.outboundAuthenticate(code);
        return ApiResponse.success(result);
    }

    @PostMapping("/introspect")
    ApiResponse<IntrospectResponse> authenticate(@RequestBody IntrospectRequest request) {
        var result = authenticationService.introspect(request);
        return ApiResponse.success(result);
    }

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

    @PostMapping("/verify-otp")
    ApiResponse<AuthenticationResponse> verifyOtp(@RequestBody VerifyOtpRequest request) {
        var result = authenticationService.verifyOtp(request);
        return ApiResponse.success(result);
    }
}
