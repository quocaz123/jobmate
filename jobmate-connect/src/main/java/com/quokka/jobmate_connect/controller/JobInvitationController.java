package com.quokka.jobmate_connect.controller;

import com.quokka.jobmate_connect.dto.ApiResponse;
import com.quokka.jobmate_connect.dto.request.invatation.JobInvitationRequest;
import com.quokka.jobmate_connect.dto.response.invatation.JobInvitationResponse;
import com.quokka.jobmate_connect.service.JobInvitationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/invitations")
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class JobInvitationController {
    JobInvitationService jobInvitationService;

    @PostMapping
    ApiResponse<JobInvitationResponse> send(@Valid @RequestBody JobInvitationRequest req) {
        return ApiResponse.success(jobInvitationService.sendInvitation(req));
    }

    @PostMapping("/{id}/accept")
    ApiResponse<JobInvitationResponse> accept(@PathVariable UUID id) {
        return ApiResponse.success(jobInvitationService.acceptInvitation(id));
    }

    @PostMapping("/{id}/reject")
    ApiResponse<JobInvitationResponse> reject(@PathVariable UUID id) {
        return ApiResponse.success(jobInvitationService.rejectInvitation(id));
    }

    @GetMapping("/received")
    ApiResponse<List<JobInvitationResponse>> getMyInvitations() {
        return ApiResponse.success(jobInvitationService.getMyInvitations());
    }

    @GetMapping("/sent")
    ApiResponse<List<JobInvitationResponse>> getSentInvitations() {
        return ApiResponse.success(jobInvitationService.getSentInvitations());
    }
}
