package com.quokka.jobmate_connect.service.maching;

import com.quokka.jobmate_connect.entity.Job;
import com.quokka.jobmate_connect.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class MatchingService {

    /**
     * Hàm chính: tính độ phù hợp giữa user và job (0-100%)
     */
    public double calculateMatchScore(User user, Job job) {
        double score = 0;
        double maxScore = 100;

        try {
            // 1️⃣ Chuẩn hóa kỹ năng
            List<String> userSkills = normalizeSkills(user.getSkills());
            List<String> jobSkills = normalizeSkills(job.getSkills());

            if (!jobSkills.isEmpty()) {
                long matched = userSkills.stream()
                        .filter(skill -> jobSkills.stream().anyMatch(js -> fuzzyMatch(skill, js)))
                        .count();

                double skillScore = ((double) matched / jobSkills.size()) * 40;
                score += Math.min(skillScore, 40);
            }

            // 2️⃣ Loại việc (Full-time / Part-time / Freelance)
            if (equalsIgnoreCaseSafe(user.getPreferredJobType(), job.getJobType().name())) {
                score += 10;
            }

            // 3️⃣ Địa điểm (theo khoảng cách bán kính)
            double distance = calculateDistance(
                    user.getLatitude(), user.getLongitude(),
                    job.getLatitude(), job.getLongitude()
            );
            if (distance <= 5.0) score += 10;      // cùng khu vực
            else if (distance <= 10.0) score += 5; // gần khu vực

            // 4️⃣ Thời gian rảnh
            if (isTimeCompatible(user.getAvailableDays(), job.getWorkingDays(), user.getAvailableTime(), job.getWorkingHours())) {
                score += 20;
            }

            // 5️⃣ Lương
            if (user.getPreferredMinSalary() != null && job.getSalary() != null) {
                if (job.getSalary().compareTo(user.getPreferredMinSalary()) >= 0) {
                    score += 10;
                } else {
                    BigDecimal diff = user.getPreferredMinSalary().subtract(job.getSalary());
                    if (diff.doubleValue() < 1000000) score += 5; // chênh lệch nhỏ vẫn cộng điểm
                }
            }

            // 6️⃣ Uy tín / Đánh giá
            if (user.getTrustScore() != null) {
                score += Math.min(user.getTrustScore() * 5, 5);
            }

        } catch (Exception e) {
            log.error("Lỗi khi tính điểm matching: {}", e.getMessage());
        }

        return Math.round(Math.min(score, maxScore) * 10.0) / 10.0; // ví dụ: 92.3
    }

    // 🔹 Chuẩn hóa kỹ năng
    private List<String> normalizeSkills(String skills) {
        if (skills == null || skills.isBlank()) return List.of();
        return Arrays.stream(skills.split(","))
                .map(String::trim)
                .map(String::toLowerCase)
                .filter(s -> !s.isBlank())
                .collect(Collectors.toList());
    }

    // 🔹 So khớp gần đúng (Levenshtein)
    private boolean fuzzyMatch(String a, String b) {
        int distance = levenshteinDistance(a, b);
        return distance <= 2 || a.contains(b) || b.contains(a);
    }

    private int levenshteinDistance(String a, String b) {
        int[][] dp = new int[a.length() + 1][b.length() + 1];
        for (int i = 0; i <= a.length(); i++) dp[i][0] = i;
        for (int j = 0; j <= b.length(); j++) dp[0][j] = j;
        for (int i = 1; i <= a.length(); i++) {
            for (int j = 1; j <= b.length(); j++) {
                int cost = (a.charAt(i - 1) == b.charAt(j - 1)) ? 0 : 1;
                dp[i][j] = Math.min(Math.min(
                                dp[i - 1][j] + 1,
                                dp[i][j - 1] + 1),
                        dp[i - 1][j - 1] + cost);
            }
        }
        return dp[a.length()][b.length()];
    }

    // 🔹 Địa lý - Haversine formula
    private double calculateDistance(Double lat1, Double lon1, Double lat2, Double lon2) {
        if (lat1 == null || lon1 == null || lat2 == null || lon2 == null) return 9999.0;
        double R = 6371; // km
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c; // km
    }

    // 🔹 So sánh an toàn (null-safe)
    private boolean equalsIgnoreCaseSafe(String a, String b) {
        return a != null && b != null && a.equalsIgnoreCase(b);
    }

    // 🔹 Kiểm tra thời gian làm việc tương thích
    private boolean isTimeCompatible(String userDays, String jobDays, String userTime, String jobTime) {
        if (userDays == null || jobDays == null) return false;
        Set<String> userSet = new HashSet<>(Arrays.asList(userDays.toLowerCase().split(",")));
        Set<String> jobSet = new HashSet<>(Arrays.asList(jobDays.toLowerCase().split(",")));
        userSet.retainAll(jobSet);
        return !userSet.isEmpty();
    }
}
