package com.quokka.jobmate_connect.service;

import com.quokka.jobmate_connect.dto.PageResponse;
import com.quokka.jobmate_connect.dto.request.rating.RatingRequest;
import com.quokka.jobmate_connect.dto.response.rating.RatingResponse;
import com.quokka.jobmate_connect.dto.response.rating.RatingStatsResponse;
import com.quokka.jobmate_connect.entity.Job;
import com.quokka.jobmate_connect.entity.Rating;
import com.quokka.jobmate_connect.entity.User;
import com.quokka.jobmate_connect.exception.AppException;
import com.quokka.jobmate_connect.exception.ErrorCode;
import com.quokka.jobmate_connect.mapper.RatingMapper;
import com.quokka.jobmate_connect.repository.JobRepository;
import com.quokka.jobmate_connect.repository.RatingRepository;
import com.quokka.jobmate_connect.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RatingService {
    RatingRepository ratingRepository;
    RatingMapper ratingMapper;
    JobRepository jobRepository;
    UserRepository userRepository;

    public RatingResponse createRating(RatingRequest request) {
        User currentUser = getCurrentUser();
        User targetUser = userRepository.findById(request.getToUserId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

       if(currentUser.getId().equals(targetUser.getId())) {
           throw new AppException(ErrorCode.CANNOT_RATE_SELF); }

        if (request.getJobId() != null) {
               if (ratingRepository.existsByFromUserIdAndToUserIdAndJobId(
                       currentUser.getId(), targetUser.getId(), request.getJobId())) {
                   throw new AppException(ErrorCode.ALREADY_RATED);
               }
           }

        if(request.getScore() < 1.0 || request.getScore() > 5.0) {
            throw new AppException(ErrorCode.INVALID_RATING_SCORE);
        }


        Job job = jobRepository.findById(request.getJobId())
                .orElseThrow(() -> new AppException(ErrorCode.JOB_NOT_FOUND));


        Rating rating = Rating.builder()
                .fromUser(currentUser)
                .toUser(targetUser)
                .job(job)
                .score(request.getScore())
                .comment(request.getComment())
                .build();

        Rating saves = ratingRepository.save(rating);

        updateTrustScore(targetUser.getId());
        return ratingMapper.toRatingResponse(saves);

    }

    public RatingResponse getRatingById(UUID ratingId) {
        Rating rating = ratingRepository.findById(ratingId)
                .orElseThrow(() -> new AppException(ErrorCode.RATING_NOT_FOUND));
        return ratingMapper.toRatingResponse(rating);
    }

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

    public RatingStatsResponse getUserRatingStatus(UUID userId) {
        Double averageRating = ratingRepository.getAverageRatingByUserId(userId);
        Long totalRatings = ratingRepository.countByToUserId(userId);

        List<Object[]> ratingStats = ratingRepository.getRatingStatsByUserId(userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        return RatingStatsResponse.builder()
                .averageRating(averageRating != null ? averageRating : 0.0)
                .totalRatings(totalRatings)
                .ratingDistribution(convertRatingStats(ratingStats))
                .badgeLevel(user.getBadgeLevel())
                .trustScore(user.getTrustScore())
                .build();
    }

    public PageResponse<RatingResponse> getMyRating(int page, int size) {
        User user = getCurrentUser();
        Pageable pageable = PageRequest.of(page, size);
        Page<Rating> ratings = ratingRepository.findByFromUserIdOrderByCreatedAtDesc(
                user.getId(), pageable
        );

        return PageResponse.<RatingResponse>builder()
                .currentPage(ratings.getNumber())
                .pageSize(size)
                .totalPages(ratings.getTotalPages())
                .totalElements(ratings.getTotalElements())
                .data(ratings.map(ratingMapper::toRatingResponse).getContent())
                .build();
    }

    public RatingResponse updateRating(UUID ratingId, RatingRequest request) {
        Rating rating = ratingRepository.findById(ratingId)
                .orElseThrow(() -> new AppException(ErrorCode.RATING_NOT_FOUND));

        User currentUser = getCurrentUser();

        // Chỉ người tạo rating mới được update
        if (!rating.getFromUser().getId().equals(currentUser.getId())) {
            throw new AppException(ErrorCode.FORBIDDEN);
        }


        if (request.getScore() < 1.0f || request.getScore() > 5.0f) {
            throw new AppException(ErrorCode.INVALID_RATING_SCORE);
        }

        rating.setScore(request.getScore());
        rating.setComment(request.getComment());

        rating = ratingRepository.save(rating);

        updateTrustScore(rating.getToUser().getId());

        return ratingMapper.toRatingResponse(rating);
    }

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

    }

    public void updateTrustScore(UUID userId) {
        Double averageRating = ratingRepository.getAverageRatingByUserId(userId);
        Long ratingCount = ratingRepository.countByToUserId(userId);

        if (averageRating != null && ratingCount > 0) {
            float trustScore = calculateTrustScore(averageRating, ratingCount);

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

            user.setTrustScore(trustScore);
            user.setReviewCount(ratingCount.intValue());
            userRepository.save(user);

            updateBadgeLevel(user, trustScore);
        }
    }
    public float calculateTrustScore(Double averageRating, Long ratingCount) {
        float ratingCountFactor = Math.min(ratingCount.floatValue() / 10.0f, 1.0f);
        return (float) (averageRating * 0.7 + ratingCountFactor * 0.3);
    }

    public void updateBadgeLevel(User user, float trustScore) {
        if (trustScore >= 4.5) {
            user.setBadgeLevel("Gold");
        } else if (trustScore >= 3.5) {
            user.setBadgeLevel("Silver");
        } else if (trustScore >= 2.5) {
            user.setBadgeLevel("Bronze");
        } else {
            user.setBadgeLevel("None");
        }
        userRepository.save(user);
    }

    public List<Map<String, Object>> convertRatingStats(List<Object[]> ratingStats) {
        return ratingStats.stream()
                .map(stat -> {
                    Map<String, Object> statMap = new HashMap<>();
                    statMap.put("score", stat[0]);
                    statMap.put("count", stat[1]);
                    return statMap;
                })
                .toList();
    }

    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
    }
}
