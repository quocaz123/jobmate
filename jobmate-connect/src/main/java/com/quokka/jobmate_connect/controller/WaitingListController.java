package com.quokka.jobmate_connect.controller;

import com.quokka.jobmate_connect.dto.ApiResponse;
import com.quokka.jobmate_connect.dto.PageResponse;
import com.quokka.jobmate_connect.dto.request.waitinglist.CreateWaitingListRequest;
import com.quokka.jobmate_connect.dto.response.waitinglist.WaitingListResponse;
import com.quokka.jobmate_connect.service.WaitingListService;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/waiting-list")
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class WaitingListController {
    WaitingListService waitingListService;

    @PostMapping
    public ApiResponse<WaitingListResponse> create(@RequestBody CreateWaitingListRequest request) {
        return ApiResponse.success(waitingListService.create(request));
    }

    @GetMapping("/my-waiting")
    public ApiResponse<List<WaitingListResponse>> getMyWaitingLists() {
        return ApiResponse.success(waitingListService.getMyWaitingList());
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteWaitingList(@PathVariable UUID id) {
        waitingListService.close(id);
        return ApiResponse.success(null);
    }


    @PreAuthorize("hasAnyRole('EMPLOYER', 'ADMIN')")
    @GetMapping("/candidates")
    public ApiResponse<PageResponse<WaitingListResponse>> getActiveCandidates(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.success(waitingListService.getActiveCandidates(page, size));
    }

}
