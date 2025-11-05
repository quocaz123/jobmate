package com.quokka.jobmate_connect.controller;

import com.quokka.jobmate_connect.dto.ApiResponse;
import com.quokka.jobmate_connect.dto.PageResponse;
import com.quokka.jobmate_connect.dto.request.rating.RatingRequest;
import com.quokka.jobmate_connect.dto.response.rating.RatingResponse;
import com.quokka.jobmate_connect.dto.response.rating.RatingStatsResponse;
import com.quokka.jobmate_connect.service.RatingService;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/ratings")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RatingController {
    RatingService ratingService;

    @PostMapping
    public ApiResponse<RatingResponse> createRating(@RequestBody RatingRequest request) {
        return ApiResponse.success(ratingService.createRating(request));
    }

    @GetMapping("/{ratingId}")
    public ApiResponse<RatingResponse> getRatingById(@PathVariable UUID ratingId) {
        return ApiResponse.success(ratingService.getRatingById(ratingId));
    }

    // Lấy danh sách đánh giá mà user NHẬN được
    @GetMapping("/user/{userId}")
    public ApiResponse<PageResponse<RatingResponse>> getUserRatings(
            @PathVariable UUID userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        PageResponse<RatingResponse> response = ratingService.getUserRatings(userId, page, size);
        return ApiResponse.success(response);
    }

    @GetMapping("/my-ratings")
    public ApiResponse<PageResponse<RatingResponse>> getMyRatings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.success(ratingService.getMyRating(page, size));
    }

    @GetMapping("/user/{userId}/stats")
    public ApiResponse<RatingStatsResponse> getUserRatingStats(@PathVariable UUID userId) {
        RatingStatsResponse response = ratingService.getUserRatingStatus(userId);
        return ApiResponse.success(response);
    }


    @PutMapping("/{ratingId}")
    public ApiResponse<RatingResponse> updateRating(
            @PathVariable UUID ratingId,
            @RequestBody RatingRequest request) {
        return ApiResponse.success(ratingService.updateRating(ratingId, request));
    }

    @DeleteMapping("/{ratingId}")
    public ApiResponse<Void> deleteRating(@PathVariable UUID ratingId) {
        ratingService.deleteRating(ratingId);
        return ApiResponse.success(null);
    }

}
