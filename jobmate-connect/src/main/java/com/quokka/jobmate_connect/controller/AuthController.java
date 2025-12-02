package com.quokka.jobmate_connect.controller;

import com.nimbusds.jose.JOSEException;
import com.quokka.jobmate_connect.dto.ApiResponse;
import com.quokka.jobmate_connect.dto.request.user.AuthenticationRequest;
import com.quokka.jobmate_connect.dto.request.user.IntrospectRequest;
import com.quokka.jobmate_connect.dto.request.user.LogoutRequest;
import com.quokka.jobmate_connect.dto.request.otp.VerifyOtpRequest;
import com.quokka.jobmate_connect.dto.request.otp.ResendOtpRequest;
import com.quokka.jobmate_connect.dto.request.user.ForgotPasswordRequest;
import com.quokka.jobmate_connect.dto.request.user.ResetPasswordRequest;
import com.quokka.jobmate_connect.dto.request.user.SetPasswordRequest;
import com.quokka.jobmate_connect.dto.response.user.AuthenticationResponse;
import com.quokka.jobmate_connect.dto.response.user.ForgotPasswordResponse;
import com.quokka.jobmate_connect.dto.response.user.IntrospectResponse;
import com.quokka.jobmate_connect.dto.response.otp.ResendOtpResponse;
import com.quokka.jobmate_connect.dto.response.user.ResetPasswordResponse;
import com.quokka.jobmate_connect.dto.response.user.SetPasswordResponse;
import com.quokka.jobmate_connect.service.AuthenticationService;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

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
    ApiResponse<IntrospectResponse> authenticate(@RequestBody IntrospectRequest request) throws ParseException {
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

    @PostMapping("/resend-otp")
    ApiResponse<ResendOtpResponse> resendOtp(@RequestBody ResendOtpRequest request) {
        var result = authenticationService.resendOtp(request.getUserId());
        return ApiResponse.success(result);
    }

    @PostMapping("/set-password")
    ApiResponse<SetPasswordResponse> setPassword(@RequestParam("userId") String userId,
            @RequestBody SetPasswordRequest request) {
        var result = authenticationService.setPassword(userId, request);
        return ApiResponse.success(result);
    }

    @PostMapping("/forgot-password")
    ApiResponse<ForgotPasswordResponse> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        var result = authenticationService.forgotPassword(request);
        return ApiResponse.success(result);
    }

    @PostMapping("/reset-password")
    ApiResponse<ResetPasswordResponse> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        var result = authenticationService.resetPassword(request);
        return ApiResponse.success(result);
    }
}
