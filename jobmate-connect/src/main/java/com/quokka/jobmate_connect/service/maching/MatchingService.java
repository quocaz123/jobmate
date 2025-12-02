package com.quokka.jobmate_connect.service.maching;

import com.quokka.jobmate_connect.entity.Job;
import com.quokka.jobmate_connect.entity.User;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MatchingService {

    MatchingEngine matchingEngine;
    SkillSynonymService skillSynonymService;

    /**
     * Hàm chính: tính độ phù hợp giữa user và job (0-100%)
     */
    public double calculateMatchScore(User user, Job job) {
        double score = 0;
        double maxScore = 100;

        try {
            // 1️⃣ Kỹ năng - Tận dụng SkillSynonymService từ MatchingEngine
            double skillScore = calculateSkillScore(user.getSkills(), job.getSkills());
            score += skillScore;

            // 2️⃣ Loại việc (Full-time / Part-time / Freelance)
            if (equalsIgnoreCaseSafe(user.getPreferredJobType(), job.getJobType().name())) {
                score += 10;
            }

            // 3️⃣ Địa điểm (theo khoảng cách bán kính)
            double distance = calculateDistance(
                    user.getLatitude(), user.getLongitude(),
                    job.getLatitude(), job.getLongitude());
            if (distance <= 5.0)
                score += 10; // cùng khu vực
            else if (distance <= 10.0)
                score += 5; // gần khu vực

            // 4️⃣ Thời gian rảnh - Tận dụng logic từ MatchingEngine
            double scheduleScore = calculateScheduleScore(
                    user.getAvailableDays(), user.getAvailableTime(),
                    job.getWorkingDays(), job.getWorkingHours());
            score += scheduleScore;

            // 5️⃣ Lương - Tận dụng normalizeSalary từ MatchingEngine
            double salaryScore = calculateSalaryScore(user, job);
            score += salaryScore;

            // 6️⃣ Uy tín / Đánh giá
            if (user.getTrustScore() != null) {
                score += Math.min(user.getTrustScore() * 5, 5);
            }

        } catch (Exception e) {
            log.error("Lỗi khi tính điểm matching: {}", e.getMessage());
        }

        return Math.round(Math.min(score, maxScore) * 10.0) / 10.0; // ví dụ: 92.3
    }

    // 🔹 Tính điểm kỹ năng - Tận dụng SkillSynonymService từ MatchingEngine
    private double calculateSkillScore(String userSkills, String jobSkills) {
        if (userSkills == null || jobSkills == null || userSkills.isBlank() || jobSkills.isBlank()) {
            return 0;
        }

        String jobNorm = skillSynonymService.normalize(jobSkills);
        int matchedCount = 0;
        String[] skills = userSkills.split("[;,]");

        for (String s : skills) {
            String skillNorm = skillSynonymService.normalize(s.trim());
            if (skillNorm.isEmpty())
                continue;

            // Direct match
            if (jobNorm.contains(skillNorm)) {
                matchedCount++;
                continue;
            }

            // Synonym match
            List<String> syns = skillSynonymService.getSynonymsSmart(skillNorm);
            if (syns != null) {
                for (String syn : syns) {
                    if (jobNorm.contains(syn) || skillSynonymService.fuzzyMatchInText(jobNorm, syn)) {
                        matchedCount++;
                        break;
                    }
                }
            }
        }

        // Tính điểm dựa trên tỷ lệ match (tối đa 40 điểm)
        if (skills.length > 0) {
            double ratio = (double) matchedCount / skills.length;
            return Math.min(ratio * 40, 40);
        }
        return 0;
    }

    // 🔹 Địa lý - Haversine formula
    private double calculateDistance(Double lat1, Double lon1, Double lat2, Double lon2) {
        if (lat1 == null || lon1 == null || lat2 == null || lon2 == null)
            return 9999.0;
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

    // 🔹 Tính điểm schedule - Tận dụng logic từ MatchingEngine
    private double calculateScheduleScore(String userDays, String userTime, String jobDays, String jobTime) {
        if (userDays == null || jobDays == null)
            return 0;
        if (userTime == null || jobTime == null)
            return 0;

        List<String> jobDaysList = Arrays.stream(jobDays.split(",")).map(String::trim).toList();
        List<String> userDaysList = Arrays.stream(userDays.split(",")).map(String::trim).toList();
        boolean okDay = jobDaysList.stream().anyMatch(userDaysList::contains);

        boolean okTime;
        try {
            String[] jt = jobTime.split("-");
            String[] ut = userTime.split("-");

            LocalTime jobFrom = LocalTime.parse(jt[0].trim());
            LocalTime jobTo = LocalTime.parse(jt[1].trim());
            LocalTime userFrom = LocalTime.parse(ut[0].trim());
            LocalTime userTo = LocalTime.parse(ut[1].trim());

            // Kiểm tra overlap: user time không nằm ngoài job time
            okTime = !(userFrom.isAfter(jobTo) || userTo.isBefore(jobFrom));
        } catch (Exception e) {
            okTime = false;
        }

        // Tương tự MatchingEngine: cả day và time = 20 điểm, chỉ 1 = 10 điểm
        if (okDay && okTime)
            return 20;
        if (okDay || okTime)
            return 10;
        return 0;
    }

    // 🔹 Tính điểm lương - Tận dụng normalizeSalary từ MatchingEngine
    private double calculateSalaryScore(User user, Job job) {
        if (user.getPreferredMinSalary() == null || job.getSalary() == null || job.getSalaryUnit() == null) {
            return 0;
        }

        // Nếu user không có preferredSalaryUnit, mặc định là VND_PER_HOUR
        String userSalaryUnit = user.getPreferredSalaryUnit() != null
                ? user.getPreferredSalaryUnit().name()
                : "VND_PER_HOUR";

        // Normalize cả 2 về per hour để so sánh
        double userExpectedPerHour = matchingEngine.normalizeSalary(
                user.getPreferredMinSalary().doubleValue(),
                userSalaryUnit);

        double jobPerHour = matchingEngine.normalizeSalary(
                job.getSalary().doubleValue(),
                job.getSalaryUnit().name());

        if (jobPerHour >= userExpectedPerHour)
            return 10;
        if (jobPerHour >= userExpectedPerHour * 0.8)
            return 5; // gần đạt
        return 0;
    }
}
