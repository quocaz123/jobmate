package com.quokka.jobmate_connect.controller;

import com.quokka.jobmate_connect.dto.ApiResponse;
import com.quokka.jobmate_connect.dto.PageResponse;
import com.quokka.jobmate_connect.dto.request.rating.RatingRequest;
import com.quokka.jobmate_connect.dto.response.rating.RatingResponse;
import com.quokka.jobmate_connect.dto.response.rating.RatingStatsResponse;
import com.quokka.jobmate_connect.service.RatingService;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/ratings")
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class RatingController {

    RatingService ratingService;

    // ----------------------------------------------------
    // Tạo đánh giá
    // ----------------------------------------------------
    @PostMapping
    public ApiResponse<RatingResponse> createRating(@RequestBody RatingRequest request) {
        return ApiResponse.success(ratingService.createRating(request));
    }

    // ----------------------------------------------------
    // Lấy chi tiết 1 đánh giá
    // ----------------------------------------------------
    @GetMapping("/{id}")
    public ApiResponse<RatingResponse> getRating(@PathVariable UUID id) {
        return ApiResponse.success(ratingService.getRatingById(id));
    }

    // ----------------------------------------------------
    // Lấy danh sách đánh giá của 1 user (ví dụ ứng viên)
    // ----------------------------------------------------
    @GetMapping("/user/{userId}")
    public ApiResponse<PageResponse<RatingResponse>> getUserRatings(
            @PathVariable UUID userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ApiResponse.success(ratingService.getUserRatings(userId, page, size));
    }

    // ----------------------------------------------------
    // Lấy thống kê trung bình & huy hiệu
    // ----------------------------------------------------
    @GetMapping("/stats/{userId}")
    public ApiResponse<RatingStatsResponse> getUserRatingStats(@PathVariable UUID userId) {
        return ApiResponse.success(ratingService.getUserRatingStatus(userId));
    }

    // ----------------------------------------------------
    // Lấy danh sách đánh giá mình đã tạo
    // ----------------------------------------------------
    @GetMapping("/me")
    public ApiResponse<PageResponse<RatingResponse>> getMyRatings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ApiResponse.success(ratingService.getMyRatings(page, size));
    }



    // ----------------------------------------------------
    // Xóa rating của mình
    // ----------------------------------------------------
    @DeleteMapping("/{ratingId}")
    public ApiResponse<Void> deleteRating(@PathVariable UUID ratingId) {
        ratingService.deleteRating(ratingId);
        return ApiResponse.success(null);
    }
}
