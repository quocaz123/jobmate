package com.quokka.jobmate_connect.service;

import com.quokka.jobmate_connect.constant.ApplicationStatus;
import com.quokka.jobmate_connect.constant.JobStatus;
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

    @Transactional
    public RatingResponse createRating(RatingRequest request) {

        User currentUser = getCurrentUser(); // người đánh giá
        User targetUser = userRepository.findById(request.getToUserId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        if (currentUser.getId().equals(targetUser.getId())) {
            throw new AppException(ErrorCode.CANNOT_RATE_SELF);
        }

        if (request.getScore() < 1.0 || request.getScore() > 5.0) {
            throw new AppException(ErrorCode.INVALID_RATING_SCORE);
        }

        Job job = null;

        if (request.getJobId() != null) {

            job = jobRepository.findById(request.getJobId())
                    .orElseThrow(() -> new AppException(ErrorCode.JOB_NOT_FOUND));

            if (!JobStatus.CLOSED.equals(job.getStatus())) {
                throw new AppException(ErrorCode.RATING_NOT_ALLOWED_BEFORE_DEADLINE);
            }

            if (ratingRepository.existsByFromUserIdAndToUserIdAndJobId(
                    currentUser.getId(),
                    targetUser.getId(),
                    job.getId())) {
                throw new AppException(ErrorCode.ALREADY_RATED);
            }

            boolean hasValidApplication = checkValidRatingRelationship(currentUser, targetUser, job);

            if (!hasValidApplication) {
                throw new AppException(ErrorCode.RATING_NOT_ALLOWED);
            }
        }

        Rating rating = Rating.builder()
                .fromUser(currentUser)
                .toUser(targetUser)
                .job(job)
                .score(request.getScore())
                .comment(request.getComment())
                .build();

        Rating saved = ratingRepository.save(rating);

        // Cập nhật TrustScore + Badge
        updateTrustScore(targetUser.getId());

        return ratingMapper.toRatingResponse(saved);
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
                .totalRatings(totalRatings != null ? totalRatings : 0L)
                .ratingDistribution(ratingStats != null ? convertRatingStats(ratingStats) : List.of())
                .badgeLevel(user.getBadgeLevel())
                .trustScore(user.getTrustScore())
                .build();
    }

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

    }

    @Transactional
    public void updateTrustScore(UUID userId) {
        Double avg = ratingRepository.getAverageRatingByUserId(userId);
        Long count = ratingRepository.countByToUserId(userId);

        if (avg == null || count == null || count == 0)
            return;

        float trustScore = calculateTrustScore(avg, count);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        user.setTrustScore(trustScore);
        user.setReviewCount(count.intValue());
        user.setBadgeLevel(getBadgeLevel(trustScore));
        userRepository.save(user);

    }

    public float calculateTrustScore(Double averageRating, Long ratingCount) {
        float ratingCountFactor = Math.min(ratingCount.floatValue() / 10.0f, 1.0f);
        float trust = (float) (averageRating * 0.7 + ratingCountFactor * 0.3);
        return Math.min(trust, 5.0f);
    }

    public String getBadgeLevel(float trustScore) {
        if (trustScore >= 4.5)
            return "Gold";
        if (trustScore >= 3.5)
            return "Silver";
        if (trustScore >= 2.5)
            return "Bronze";
        return "None";
    }

    public List<Map<String, Object>> convertRatingStats(List<Object[]> ratingStats) {
        return ratingStats.stream()
                .map(stat -> Map.of("score", stat[0], "count", stat[1]))
                .toList();
    }

    public User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
    }

    private boolean checkValidRatingRelationship(User currentUser, User targetUser, Job job) {
        if (job == null || job.getCreatedBy() == null) {
            return false;
        }

        UUID jobOwnerId = job.getCreatedBy().getId();
        UUID currentUserId = currentUser.getId();
        UUID targetUserId = targetUser.getId();

        boolean currentUserHasApplication = applicationRepository.existsByUserIdAndJobIdAndStatusIn(
                currentUserId,
                job.getId(),
                List.of(ApplicationStatus.ACCEPTED, ApplicationStatus.REJECTED));

        if (currentUserHasApplication) {
            boolean jobBelongsToTarget = jobOwnerId.equals(targetUserId);

            if (jobBelongsToTarget) {
                return true;
            }
        }

        boolean targetUserHasApplication = applicationRepository.existsByUserIdAndJobIdAndStatusIn(
                targetUserId,
                job.getId(),
                List.of(ApplicationStatus.ACCEPTED, ApplicationStatus.REJECTED));

        if (targetUserHasApplication) {
            boolean jobBelongsToCurrent = jobOwnerId.equals(currentUserId);

            if (jobBelongsToCurrent) {
                return true;
            }
        }

        return false;
    }

}
