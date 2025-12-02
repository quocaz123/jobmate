package com.quokka.jobmate_connect.service.maching;

import com.quokka.jobmate_connect.dto.response.job.JobESResponse;
import com.quokka.jobmate_connect.entity.Job;
import com.quokka.jobmate_connect.entity.User;
import com.quokka.jobmate_connect.entity.WaitingList;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MatchingEngine {

    SkillSynonymService skillSynonymService;

    public double calculateScoreJobForUser(User user, WaitingList wl, JobESResponse job) {

        double score = 0;

        score += skillScore(wl.getSkills(), job.getSkills());
        score += salaryScore(wl, job);
        score += distanceScore(job.getDistance(), wl.getSearchRadius());
        score += scheduleScore(wl, job);

        return score;
    }

    public double calculateScoreUserForJob(Job job, WaitingList wl, double distance) {

        double score = 0;

        score += skillScore(wl.getSkills(), job.getSkills());
        score += salaryScoreForJob(job, wl);
        score += distanceScore(distance, wl.getSearchRadius());
        score += scheduleScoreForJob(job, wl);

        return score;
    }

    // private double skillScore(String userSkills, String jobSkills) {
    // if (userSkills == null || jobSkills == null) return 0;
    //
    // int score = 0;
    // String[] skillArray = userSkills.split("[;,]");
    //
    // for (String s : skillArray) {
    // String skill = s.trim().toLowerCase();
    // if (!skill.isEmpty() && jobSkills.toLowerCase().contains(skill))
    // score += 10;
    // }
    //
    // return Math.min(score, 50);
    // }

    private double skillScore(String userSkills, String jobSkills) {
        if (userSkills == null || jobSkills == null)
            return 0;

        String jobNorm = skillSynonymService.normalize(jobSkills);

        int score = 0;
        String[] skills = userSkills.split("[;,]");

        for (String s : skills) {
            String skillNorm = skillSynonymService.normalize(s);

            // √ Direct match
            if (jobNorm.contains(skillNorm)) {
                score += 10;
                continue;
            }

            // √ Improve: lấy synonyms thông minh
            List<String> syns = skillSynonymService.getSynonymsSmart(skillNorm);

            if (syns != null) {
                for (String syn : syns) {
                    if (jobNorm.contains(syn) ||
                            skillSynonymService.fuzzyMatchInText(jobNorm, syn)) {
                        score += 10;
                        break;
                    }
                }
            }
        }

        return Math.min(score, 50);
    }

    public double salaryScore(WaitingList wl, JobESResponse job) {
        if (wl.getExpectedMinSalary() == null || job.getSalaryPerHour() == null)
            return 0;

        double expect = normalizeSalary(
                wl.getExpectedMinSalary().doubleValue(),
                wl.getExpectedSalaryUnit().name()
        );

        double actual = job.getSalaryPerHour();

        if (actual >= expect) return 20;
        if (actual >= expect * 0.8) return 10; // gần đạt
        return 0;
    }

    public double salaryScoreForJob(Job job, WaitingList wl) {
        if (wl.getExpectedMinSalary() == null || job.getSalary() == null)
            return 0;

        double actual = normalizeSalary(
                job.getSalary().doubleValue(),
                job.getSalaryUnit().name());

        double expect = normalizeSalary(
                wl.getExpectedMinSalary().doubleValue(),
                wl.getExpectedSalaryUnit().name());

        return actual >= expect ? 20 : 5;
    }

    public double distanceScore(double distance, Integer radius) {
        if (radius == null)
            return 0;
        if (distance <= radius)
            return 20;
        if (distance <= radius + 3)
            return 10;
        return 0;
    }

    public double scheduleScore(WaitingList wl, JobESResponse job) {
        if (wl.getAvailableDays() == null || job.getScheduleDays() == null)
            return 0;
        if (wl.getAvailableTime() == null || job.getScheduleTime() == null)
            return 0;

        List<String> jobDays = Arrays.stream(job.getScheduleDays().split(",")).map(String::trim).toList();
        List<String> wlDays = Arrays.stream(wl.getAvailableDays().split(",")).map(String::trim).toList();
        boolean okDay = jobDays.stream().anyMatch(wlDays::contains);

        boolean okTime;
        try {
            String[] jt = job.getScheduleTime().split("-");
            String[] wt = wl.getAvailableTime().split("-");

            LocalTime jobFrom = LocalTime.parse(jt[0].trim());
            LocalTime jobTo = LocalTime.parse(jt[1].trim());
            LocalTime wlFrom = LocalTime.parse(wt[0].trim());
            LocalTime wlTo = LocalTime.parse(wt[1].trim());

            okTime = !(wlFrom.isAfter(jobTo) || wlTo.isBefore(jobFrom));
        } catch (Exception e) {
            okTime = false;
        }

        if (okDay && okTime)
            return 10;
        if (okDay || okTime)
            return 5;
        return 0;
    }

    public double scheduleScoreForJob(Job job, WaitingList wl) {
        return scheduleScore(wl, new JobESResponse(
                null, // id
                null, // title
                null, // description
                null, // jobType
                null, // salary
                null, // salaryUnit
                null, // distance
                job.getWorkingDays(), // scheduleDays
                job.getWorkingHours(), // scheduleTime
                null,
                null,
                null, // skills
                null // status
        ));
    }

    public double normalizeSalary(double salary, String salaryUnit) {

        if (salaryUnit == null) return salary;

        return switch (salaryUnit) {

            /* === Công việc phổ biến === */
            case "VND_PER_HOUR" -> salary;

            case "VND_PER_SHIFT" -> salary / 4;         // Ca 4 tiếng

            case "VND_PER_DAY" -> salary / 8;           // 8 tiếng / ngày
            case "VND_PER_WEEK" -> salary / 48;         // 6 ngày * 8 tiếng
            case "VND_PER_MONTH" -> salary / 208;       // 26 ngày * 8 tiếng

            /* === Freelancer / sản phẩm === */
            case "VND_PER_PROJECT" -> salary / 20;      // dự án ~20 giờ (ước lượng)
            case "VND_PER_PRODUCT" -> salary / 5;       // 1 sản phẩm thiết kế/chế tác ~5 giờ
            case "VND_PER_TASK" -> salary / 3;          // 1 task trung bình ~3 giờ

            /* === Dịch vụ giao hàng === */
            case "VND_PER_ORDER" -> salary / 0.3;       // đơn hàng trung bình mất 18 phút (~0.3h)
            case "VND_PER_KM" -> salary / 0.1;          // 1 km mất ~6 phút (0.1h)

            /* === Giáo dục / đào tạo === */
            case "VND_PER_SESSION" -> salary / 2;       // buổi học 2 tiếng
            case "VND_PER_STUDENT" -> salary / 1;       // tạm xem là 1 giờ / học sinh

            /* === Bán hàng / tư vấn === */
            case "COMMISSION", "BONUS" -> 0;            // không normalize được → không so sánh

            /* === Không xác định / thỏa thuận === */
            case "NEGOTIABLE" -> 0;

            /* === fallback === */
            default -> salary;
        };
    }


    // public double scheduleScore(WaitingList wl, JobESResponse job) {
    // if (wl.getAvailableDays() == null || job.getScheduleDays() == null)
    // return 0;
    //
    // boolean okDay = job.getScheduleDays().contains(wl.getAvailableDays());
    // boolean okTime = job.getScheduleTime().contains(wl.getAvailableTime());
    //
    // return (okDay && okTime) ? 10 : 0;
    // }
    //
    // public double scheduleScoreForJob(Job job, WaitingList wl) {
    // if (job.getWorkingDays() == null || wl.getAvailableDays() == null) return 0;
    // if (job.getWorkingHours() == null || wl.getAvailableTime() == null) return 0;
    //
    // List<String> jobDays = Arrays.asList(job.getWorkingDays().split("(,)"));
    // List<String> wlDays = Arrays.asList(wl.getAvailableDays().split("(,)"));
    //
    // boolean okDay = jobDays.stream().anyMatch(wlDays::contains);
    //
    // boolean okTime;
    //
    // try {
    // String [] jt = job.getWorkingHours().split("-");
    // String [] wt = wl.getAvailableTime().split("-");
    //
    // LocalTime jobFrom = LocalTime.parse(jt[0].trim());
    // LocalTime jobTo = LocalTime.parse(jt[1].trim());
    //
    // LocalTime wlFrom = LocalTime.parse(wt[0].trim());
    // LocalTime wlTo = LocalTime.parse(wt[1].trim());
    //
    // okTime = !(wlFrom.isAfter(jobTo) || wlTo.isBefore(jobFrom));
    // } catch (Exception e) {
    // okTime = false;
    // }
    //
    // if (okDay && okTime) return 10;
    // if (okDay || okTime) return 5;
    // return 0;
    //
    // }

//    public double normalizeSalary(double salary, String salaryUnit) {
//
//        if (salaryUnit == null)
//            return salary;
//
//        return switch (salaryUnit) {
//            case "VND_PER_HOUR" -> salary;
//            case "VND_PER_SHIFT" -> salary / 4; // 4h / ca
//            case "VND_PER_DAY" -> salary / 8; // 8h / ngày
//            case "VND_PER_WEEK" -> salary / 48; // 6 ngày * 8h
//            case "VND_PER_MONTH" -> salary / 208; // 26 ngày * 8h
//            case "VND_PER_SESSION" -> salary / 2; // buổi 2 tiếng
//            case "VND_PER_ORDER" -> salary; // không quy đổi chắc
//            case "VND_PER_KM" -> salary;
//            case "VND_PER_PROJECT", "VND_PER_PRODUCT", "VND_PER_TASK" ->
//                salary; // project-based không normalize
//            case "COMMISSION", "BONUS", "NEGOTIABLE" -> 0;
//            default -> salary;
//        };
//    }
}
