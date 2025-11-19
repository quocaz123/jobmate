package com.quokka.jobmate_connect.service.maching;

import com.quokka.jobmate_connect.dto.response.job.JobESResponse;
import com.quokka.jobmate_connect.entity.Job;
import com.quokka.jobmate_connect.entity.User;
import com.quokka.jobmate_connect.entity.WaitingList;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MatchingEngine {
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

    private double skillScore(String userSkills, String jobSkills) {
        if (userSkills == null || jobSkills == null) return 0;

        int score = 0;
        String[] skillArray = userSkills.split("[;,]");

        for (String s : skillArray) {
            String skill = s.trim().toLowerCase();
            if (!skill.isEmpty() && jobSkills.toLowerCase().contains(skill))
                score += 10;
        }

        return Math.min(score, 50);
    }

    public double salaryScore(WaitingList wl, JobESResponse job) {
        if (wl.getExpectedMinSalary() == null || job.getSalary() == null)
            return 0;

        double expect = normalizeSalary(
                wl.getExpectedMinSalary().doubleValue(),
                wl.getExpectedSalaryUnit().name()
        );

        double actual = normalizeSalary(
                job.getSalary(),
                job.getSalaryUnit()
        );

        return actual >= expect ? 20 : 5;
    }

    public double salaryScoreForJob(Job job, WaitingList wl) {
        if (wl.getExpectedMinSalary() == null || job.getSalary() == null)
            return 0;

        double actual = normalizeSalary(
                job.getSalary().doubleValue(),
                job.getSalaryUnit().name()
        );

        double expect = normalizeSalary(
                wl.getExpectedMinSalary().doubleValue(),
                wl.getExpectedSalaryUnit().name()
        );


        return actual >= expect ? 20 : 5;
    }

    public double distanceScore(double distance, Integer radius) {
        if (radius == null) return 0;
        if (distance <= radius) return 20;
        if (distance <= radius + 3) return 10;
        return 0;
    }


    public double scheduleScore(WaitingList wl, JobESResponse job) {
        if (wl.getAvailableDays() == null || job.getScheduleDays() == null)
            return 0;

        boolean okDay = job.getScheduleDays().contains(wl.getAvailableDays());
        boolean okTime = job.getScheduleTime().contains(wl.getAvailableTime());

        return (okDay && okTime) ? 10 : 0;
    }

    public double scheduleScoreForJob(Job job, WaitingList wl) {
        if (job.getWorkingDays() == null || wl.getAvailableDays() == null) return 0;
        if (job.getWorkingHours() == null || wl.getAvailableTime() == null) return 0;

        return (job.getWorkingDays().contains(wl.getAvailableDays()) &&
                job.getWorkingHours().contains(wl.getAvailableTime())) ? 10 : 0;
    }

    public double normalizeSalary(double salary, String salaryUnit) {

        if (salaryUnit == null) return salary;

        return switch (salaryUnit) {
            case "VND_PER_HOUR" -> salary;
            case "VND_PER_SHIFT" -> salary / 4;   // 4h / ca
            case "VND_PER_DAY" -> salary / 8;     // 8h / ngày
            case "VND_PER_WEEK" -> salary / 48;   // 6 ngày * 8h
            case "VND_PER_MONTH" -> salary / 208; // 26 ngày * 8h
            case "VND_PER_SESSION" -> salary / 2; // buổi 2 tiếng
            case "VND_PER_ORDER" -> salary;       // không quy đổi chắc
            case "VND_PER_KM" -> salary;
            case "VND_PER_PROJECT", "VND_PER_PRODUCT", "VND_PER_TASK" ->
                    salary; // project-based không normalize
            case "COMMISSION", "BONUS", "NEGOTIABLE" -> 0;
            default -> salary;
        };
    }
}
