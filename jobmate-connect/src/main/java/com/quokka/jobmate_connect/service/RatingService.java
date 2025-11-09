package com.quokka.jobmate_connect.service;

import com.quokka.jobmate_connect.constant.ApplicationStatus;
import com.quokka.jobmate_connect.dto.PageResponse;
import com.quokka.jobmate_connect.dto.request.rating.RatingRequest;
import com.quokka.jobmate_connect.dto.response.rating.RatingResponse;
import com.quokka.jobmate_connect.dto.response.rating.RatingStatsResponse;
import com.quokka.jobmate_connect.entity.Application;
import com.quokka.jobmate_connect.entity.Job;
import com.quokka.jobmate_connect.entity.Rating;
import com.quokka.jobmate_connect.entity.User;
import com.quokka.jobmate_connect.exception.AppException;
import com.quokka.jobmate_connect.exception.ErrorCode;
import com.quokka.jobmate_connect.mapper.RatingMapper;
import com.quokka.jobmate_connect.repository.ApplicationRepository;
import com.quokka.jobmate_connect.repository.JobRepository;
import com.quokka.jobmate_connect.repository.RatingRepository;
import com.quokka.jobmate_connect.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class RatingService {

    RatingRepository ratingRepository;
    RatingMapper ratingMapper;
    JobRepository jobRepository;
    UserRepository userRepository;
    ApplicationRepository applicationRepository;

    // -------------------------------------------------------
    // Tạo đánh giá mới (Rating)
    // -------------------------------------------------------
    @Transactional
    public RatingResponse createRating(RatingRequest request) {
        User currentUser = getCurrentUser();
        User targetUser = userRepository.findById(request.getToUserId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        //  Không thể tự đánh giá chính mình
        if (currentUser.getId().equals(targetUser.getId())) {
            throw new AppException(ErrorCode.CANNOT_RATE_SELF);
        }

        //  Kiểm tra điểm hợp lệ
        if (request.getScore() < 1.0 || request.getScore() > 5.0) {
            throw new AppException(ErrorCode.INVALID_RATING_SCORE);
        }

        Job job = null;
        if (request.getJobId() != null) {
            job = jobRepository.findById(request.getJobId())
                    .orElseThrow(() -> new AppException(ErrorCode.JOB_NOT_FOUND));

            //  Kiểm tra quan hệ ứng tuyển giữa 2 bên (chỉ được rating khi đã có kết quả)
            boolean hasValidApplication = applicationRepository.existsByUserIdAndJobIdAndStatusIn(
                    targetUser.getId(),
                    job.getId(),
                    List.of(ApplicationStatus.ACCEPTED, ApplicationStatus.REJECTED)
            );

            if (!hasValidApplication) {
                throw new AppException(ErrorCode.RATING_NOT_ALLOWED);
            }

            //  Chỉ cho phép đánh giá khi công việc đã "CLOSED"
            if (job.getStatus() == null || !job.getStatus().name().equalsIgnoreCase("CLOSED")) {
                throw new AppException(ErrorCode.RATING_NOT_ALLOWED_BEFORE_DEADLINE);
            }

            //  Nếu đã đánh giá rồi thì không được đánh giá lại
            if (ratingRepository.existsByFromUserIdAndToUserIdAndJobId(
                    currentUser.getId(), targetUser.getId(), job.getId())) {
                throw new AppException(ErrorCode.ALREADY_RATED);
            }
        }

        //  Tạo đánh giá mới
        Rating rating = Rating.builder()
                .fromUser(currentUser)
                .toUser(targetUser)
                .job(job)
                .score(request.getScore())
                .comment(request.getComment())
                .build();

        Rating saved = ratingRepository.save(rating);
        updateTrustScore(targetUser.getId());

        log.info(" [{}] rated [{}] for job [{}] - score: {}",
                currentUser.getEmail(),
                targetUser.getEmail(),
                job != null ? job.getTitle() : "General",
                request.getScore());

        return ratingMapper.toRatingResponse(saved);
    }

    // -------------------------------------------------------
    // Lấy 1 rating cụ thể
    // -------------------------------------------------------
    public RatingResponse getRatingById(UUID ratingId) {
        Rating rating = ratingRepository.findById(ratingId)
                .orElseThrow(() -> new AppException(ErrorCode.RATING_NOT_FOUND));
        return ratingMapper.toRatingResponse(rating);
    }

    // -------------------------------------------------------
    // Lấy danh sách đánh giá của 1 người dùng (user profile)
    // -------------------------------------------------------
    public PageResponse<RatingResponse> getUserRatings(UUID userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Rating> ratings = ratingRepository.findByToUserIdOrderByCreatedAtDesc(userId, pageable);

        return PageResponse.<RatingResponse>builder()
                .currentPage(ratings.getNumber())
                .pageSize(size)
                .totalPages(ratings.getTotalPages())
                .totalElements(ratings.getTotalElements())
                .data(ratings.map(ratingMapper::toRatingResponse).getContent())
                .build();
    }

    // -------------------------------------------------------
    // Thống kê điểm trung bình và huy hiệu của user
    // -------------------------------------------------------
    public RatingStatsResponse getUserRatingStatus(UUID userId) {
        Double averageRating = ratingRepository.getAverageRatingByUserId(userId);
        Long totalRatings = ratingRepository.countByToUserId(userId);
        List<Object[]> ratingStats = ratingRepository.getRatingStatsByUserId(userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        return RatingStatsResponse.builder()
                .averageRating(averageRating != null ? averageRating : 0.0)
                .totalRatings(totalRatings != null ? totalRatings : 0L)
                .ratingDistribution(ratingStats != null ? convertRatingStats(ratingStats) : List.of())
                .badgeLevel(user.getBadgeLevel())
                .trustScore(user.getTrustScore())
                .build();
    }

    // -------------------------------------------------------
    // Lấy danh sách rating do user hiện tại đã tạo
    // -------------------------------------------------------
    public PageResponse<RatingResponse> getMyRatings(int page, int size) {
        User user = getCurrentUser();
        Pageable pageable = PageRequest.of(page, size);
        Page<Rating> ratings = ratingRepository.findByFromUserIdOrderByCreatedAtDesc(user.getId(), pageable);

        return PageResponse.<RatingResponse>builder()
                .currentPage(ratings.getNumber())
                .pageSize(size)
                .totalPages(ratings.getTotalPages())
                .totalElements(ratings.getTotalElements())
                .data(ratings.map(ratingMapper::toRatingResponse).getContent())
                .build();
    }



    // -------------------------------------------------------
    // Xóa rating
    // -------------------------------------------------------
    @Transactional
    public void deleteRating(UUID ratingId) {
        Rating rating = ratingRepository.findById(ratingId)
                .orElseThrow(() -> new AppException(ErrorCode.RATING_NOT_FOUND));

        User currentUser = getCurrentUser();
        if (!rating.getFromUser().getId().equals(currentUser.getId())) {
            throw new AppException(ErrorCode.FORBIDDEN);
        }

        UUID targetUserId = rating.getToUser().getId();
        ratingRepository.delete(rating);
        updateTrustScore(targetUserId);

        log.info("🗑 [{}] deleted rating [{}]", currentUser.getEmail(), ratingId);
    }

    // -------------------------------------------------------
    // Cập nhật Trust Score và Badge
    // -------------------------------------------------------
    @Transactional
    public void updateTrustScore(UUID userId) {
        Double avg = ratingRepository.getAverageRatingByUserId(userId);
        Long count = ratingRepository.countByToUserId(userId);

        if (avg == null || count == null || count == 0) return;

        float trustScore = calculateTrustScore(avg, count);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        user.setTrustScore(trustScore);
        user.setReviewCount(count.intValue());
        user.setBadgeLevel(getBadgeLevel(trustScore));
        userRepository.save(user);

        log.debug("Updated trust score for [{}]: {} ({} ratings)",
                user.getEmail(), trustScore, count);
    }

    // -------------------------------------------------------
    // Helper: Tính điểm uy tín
    // -------------------------------------------------------
    public float calculateTrustScore(Double averageRating, Long ratingCount) {
        float ratingCountFactor = Math.min(ratingCount.floatValue() / 10.0f, 1.0f);
        float trust = (float) (averageRating * 0.7 + ratingCountFactor * 0.3);
        return Math.min(trust, 5.0f);
    }

    public String getBadgeLevel(float trustScore) {
        if (trustScore >= 4.5) return "Gold";
        if (trustScore >= 3.5) return "Silver";
        if (trustScore >= 2.5) return "Bronze";
        return "None";
    }

    // -------------------------------------------------------
    // Helper: Convert raw stats to map
    // -------------------------------------------------------
    public List<Map<String, Object>> convertRatingStats(List<Object[]> ratingStats) {
        return ratingStats.stream()
                .map(stat -> Map.of("score", stat[0], "count", stat[1]))
                .toList();
    }

    // -------------------------------------------------------
    // Helper: Lấy user hiện tại
    // -------------------------------------------------------
    public User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
    }
}
