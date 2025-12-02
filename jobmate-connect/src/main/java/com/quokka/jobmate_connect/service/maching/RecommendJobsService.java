package com.quokka.jobmate_connect.service.maching;

import co.elastic.clients.elasticsearch._types.query_dsl.RangeQuery;
import co.elastic.clients.json.JsonData;
import com.quokka.jobmate_connect.constant.JobType;
import com.quokka.jobmate_connect.dto.PageResponse;
import com.quokka.jobmate_connect.dto.response.job.JobESResponse;
import com.quokka.jobmate_connect.dto.response.waitinglist.WaitingListRecommendResponse;
import com.quokka.jobmate_connect.entity.Job;
import com.quokka.jobmate_connect.entity.User;
import com.quokka.jobmate_connect.entity.WaitingList;
import com.quokka.jobmate_connect.entity.eslasticsearch.JobES;
import com.quokka.jobmate_connect.exception.AppException;
import com.quokka.jobmate_connect.exception.ErrorCode;
import com.quokka.jobmate_connect.repository.JobRepository;
import com.quokka.jobmate_connect.repository.UserRepository;
import com.quokka.jobmate_connect.repository.WaitingListRepository;
import com.quokka.jobmate_connect.service.GeocodingService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class RecommendJobsService {

        private static final int DEFAULT_SEARCH_RADIUS_KM = 25;

        UserRepository userRepository;
        WaitingListRepository waitingListRepository;
        ElasticsearchTemplate elasticsearchTemplate;
        GeocodingService geocodingService;
        MatchingEngine matchingEngine;
        JobRepository jobRepository;

        public PageResponse<List<JobESResponse>> recommend(UUID waitingListId) {

                Jwt jwt = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
                UUID userId = UUID.fromString(jwt.getClaimAsString("userId"));

                User user = userRepository.findById(userId)
                                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

                WaitingList wl;
                if (waitingListId != null) {
                        wl = waitingListRepository.findById(waitingListId)
                                        .orElseThrow(() -> new AppException("Waiting list not found",
                                                        ErrorCode.NOT_FOUND));
                        if (!wl.getUser().getId().equals(userId)) {
                                throw new AppException("Waiting list không thuộc về bạn", ErrorCode.FORBIDDEN);
                        }
                } else {
                        wl = buildProfileWaitingList(user);
                }

                List<JobESResponse> jobs = recommendByWaitingList(user, wl);

                return PageResponse.<List<JobESResponse>>builder()
                                .currentPage(0)
                                .totalPages(1)
                                .pageSize(jobs.size())
                                .totalElements(jobs.size())
                                .data(List.of(jobs))
                                .build();
        }

        private WaitingList buildProfileWaitingList(User user) {
                boolean hasSkills = user.getSkills() != null && !user.getSkills().isBlank();
                boolean hasJobType = user.getPreferredJobType() != null && !user.getPreferredJobType().isBlank();
                boolean hasSalary = user.getPreferredMinSalary() != null;
                boolean hasLocation = user.getLatitude() != null && user.getLongitude() != null;

                if (!hasSkills && !hasJobType && !hasSalary && !hasLocation) {
                        throw new AppException("Vui lòng cập nhật hồ sơ để nhận gợi ý việc làm", ErrorCode.BAD_REQUEST);
                }

                Integer radius = hasLocation ? DEFAULT_SEARCH_RADIUS_KM : null;

                WaitingList.WaitingListBuilder builder = WaitingList.builder()
                                .user(user)
                                .skills(user.getSkills())
                                .expectedMinSalary(user.getPreferredMinSalary())
                                .expectedSalaryUnit(user.getPreferredSalaryUnit())
                                .availableDays(user.getAvailableDays())
                                .availableTime(user.getAvailableTime())
                                .latitude(user.getLatitude())
                                .longitude(user.getLongitude())
                                .searchRadius(radius);

                if (hasJobType) {
                        try {
                                builder.jobType(JobType.valueOf(user.getPreferredJobType()));
                        } catch (IllegalArgumentException ex) {
                                log.warn("Preferred job type {} không hợp lệ, bỏ qua", user.getPreferredJobType());
                        }
                }

                return builder.build();
        }

        private List<JobESResponse> recommendByWaitingList(User user, WaitingList wl) {

                String jobType = wl.getJobType() != null ? wl.getJobType().name() : null;

                // Không dùng minSalary cũ nữa — dùng normalizeSalary
                Double expectedMinSalary = wl.getExpectedMinSalary() != null
                                ? wl.getExpectedMinSalary().doubleValue()
                                : null;

                // ============================================
                // ES QUERY OPTIMIZED (V4)
                // ============================================
                NativeQuery query = NativeQuery.builder()
                                .withQuery(q -> q.bool(b -> {

                                        // --- JOB TYPE FILTER ---
                                        if (jobType != null) {
                                                b.must(m -> m.term(t -> t.field("jobType").value(jobType)));
                                        }

                                        // --- SKILL MATCH ---
                                        if (wl.getSkills() != null && !wl.getSkills().isBlank()) {

                                                String[] skillTokens = wl.getSkills().split("[;,]");

                                                b.must(m -> m.bool(bb -> {

                                                        for (String token : skillTokens) {
                                                                String tk = token.trim();
                                                                if (!tk.isEmpty()) {
                                                                        bb.should(s -> s.match(t -> t
                                                                                        .field("skills")
                                                                                        .query(tk)
                                                                                        .fuzziness("AUTO")));
                                                                }
                                                        }

                                                        // ít nhất 1 skill match → job được giữ lại
                                                        bb.minimumShouldMatch("1");
                                                        return bb;
                                                }));
                                        }

                                        // --- SALARY NORMALIZED FILTER (FIX) ---
                                        if (expectedMinSalary != null && expectedMinSalary > 0) {

                                                double expectedPerHour = matchingEngine.normalizeSalary(
                                                                wl.getExpectedMinSalary().doubleValue(),
                                                                wl.getExpectedSalaryUnit().name());

                                                RangeQuery rq = RangeQuery.of(r -> r.untyped(u -> u
                                                                .field("salaryPerHour")
                                                                .gte(JsonData.of(expectedPerHour))));
                                                b.filter(f -> f.range(rq));
                                        }

                                        return b;
                                }))
                                .withPageable(PageRequest.of(0, 300))
                                .build();

                var hits = elasticsearchTemplate.search(query, JobES.class);

                List<JobES> raw = hits.getSearchHits().stream()
                                .map(SearchHit::getContent)
                                .toList();

                // ===============================================
                // MAP JOB → DTO + CALCULATE DISTANCE
                // ===============================================
                List<JobESResponse> jobs = raw.stream()
                                .map(job -> {
                                        double distance = -1;
                                        // Sử dụng waiting list location thay vì user location
                                        if (job.getLocation() != null &&
                                                        wl.getLatitude() != null &&
                                                        wl.getLongitude() != null) {

                                                distance = geocodingService.calculateDistance(
                                                                wl.getLatitude(), wl.getLongitude(),
                                                                job.getLocation().getLat(), job.getLocation().getLon());
                                        }
                                        return mapToJobES(job, distance);
                                })
                                .toList();

                // ===============================================
                // FILTER RADIUS + SORT DISTANCE
                // ===============================================
                jobs = jobs.stream()
                                .filter(j -> wl.getSearchRadius() == null ||
                                                j.getDistance() == -1 ||
                                                j.getDistance() <= wl.getSearchRadius())
                                .sorted((a, b) -> Double.compare(
                                                a.getDistance() == -1 ? Double.MAX_VALUE : a.getDistance(),
                                                b.getDistance() == -1 ? Double.MAX_VALUE : b.getDistance()))
                                .toList();

                // ===============================================
                // CALCULATE SCORE → CACHE → SORT BY SCORE
                // ===============================================
                return jobs.stream()
                                .map(j -> {
                                        double score = matchingEngine.calculateScoreJobForUser(user, wl, j);
                                        j.setScore(score);
                                        return j;
                                })
                                .sorted((a, b) -> Double.compare(b.getScore(), a.getScore()))
                                .limit(20)
                                .toList();
        }

        // private List<JobESResponse> recommendByWaitingList(User user, WaitingList wl)
        // {
        //
        // String jobType = wl.getJobType() != null ? wl.getJobType().name() : null;
        //
        // Double minSalary = wl.getExpectedMinSalary() != null
        // ? wl.getExpectedMinSalary().doubleValue()
        // : null;
        //
        // NativeQuery query = NativeQuery.builder()
        // .withQuery(q -> q.bool(b -> {
        //
        // if (jobType != null) {
        // b.must(m -> m.term(t -> t.field("jobType").value(jobType)));
        // }
        //
        // if (wl.getSkills() != null && !wl.getSkills().isBlank()) {
        // b.must(m -> m.match(t -> t
        // .field("skills")
        // .query(wl.getSkills())
        // .fuzziness("AUTO")));
        // }
        //
        // if (minSalary != null && minSalary > 0) {
        // RangeQuery rq = RangeQuery.of(r -> r.untyped(u -> u
        // .field("salary").gte(JsonData.of(minSalary))));
        // b.filter(f -> f.range(rq));
        // }
        // return b;
        // }))
        // .withPageable(PageRequest.of(0, 200))
        // .build();
        //
        // var hits = elasticsearchTemplate.search(query, JobES.class);
        //
        // List<JobES> rawJobs = hits.getSearchHits().stream()
        // .map(SearchHit::getContent)
        // .toList();
        //
        // // ---- map to dto + calculate distance ----
        // List<JobESResponse> filtered = rawJobs.stream()
        // .map(job -> {
        // double distance = -1;
        //
        // if (job.getLocation() != null &&
        // user.getLatitude() != null && user.getLongitude() != null) {
        // distance = geocodingService.calculateDistance(
        // user.getLatitude(), user.getLongitude(),
        // job.getLocation().getLat(), job.getLocation().getLon());
        // }
        // return mapToJobES(job, distance);
        // })
        // .filter(dto -> wl.getSearchRadius() == null ||
        // wl.getSearchRadius() <= 0 ||
        // dto.getDistance() == -1 || // job không có location → vẫn giữ
        // dto.getDistance() <= wl.getSearchRadius())
        // .toList();
        //
        // // ---- sort theo score ----
        // filtered = filtered.stream()
        // .sorted((a, b) -> Double.compare(
        // matchingEngine.calculateScoreJobForUser(user, wl, b),
        // matchingEngine.calculateScoreJobForUser(user, wl, a)))
        // .limit(20)
        // .toList();
        //
        // return filtered;
        // }

        public List<WaitingListRecommendResponse> recommendWaitingListForJob(UUID jobId) {

                Job job = jobRepository.findById(jobId)
                                .orElseThrow(() -> new AppException(ErrorCode.JOB_NOT_FOUND));

                List<WaitingList> all = waitingListRepository.findAll();

                return all.stream()
                                .map(wl -> mapWaitingListToDto(job, wl))
                                .filter(dto -> dto.getRadius() == null ||
                                                dto.getDistance() == null ||
                                                dto.getDistance() <= dto.getRadius())
                                .sorted((a, b) -> Double.compare(b.getScore(), a.getScore()))
                                .limit(20)
                                .toList();
        }

        private WaitingListRecommendResponse mapWaitingListToDto(Job job, WaitingList wl) {

                Double distance = null;

                if (wl.getLatitude() != null && wl.getLongitude() != null &&
                                job.getLatitude() != null && job.getLongitude() != null) {

                        distance = geocodingService.calculateDistance(
                                        wl.getLatitude(), wl.getLongitude(),
                                        job.getLatitude(), job.getLongitude());
                }

                double score = matchingEngine.calculateScoreUserForJob(
                                job,
                                wl,
                                distance == null ? -1 : distance);

                return WaitingListRecommendResponse.builder()
                                .waitingListId(wl.getId().toString())
                                .userId(wl.getUser().getId().toString())
                                .fullName(wl.getUser().getFullName())
                                .skills(wl.getSkills())
                                .expectedMinSalary(wl.getExpectedMinSalary().doubleValue())
                                .expectedSalaryUnit(wl.getExpectedSalaryUnit())
                                .distance(distance)
                                .radius(wl.getSearchRadius())
                                .score(score)
                                .availableDays(wl.getAvailableDays())
                                .availableTime(wl.getAvailableTime())
                                .build();
        }

        private JobESResponse mapToJobES(JobES job, double distance) {
                return JobESResponse.builder()
                                .id(job.getId())
                                .title(job.getTitle())
                                .description(job.getDescription())
                                .jobType(job.getJobType())
                                .skills(job.getSkills())
                                .salary(job.getSalary())
                                .salaryUnit(job.getSalaryUnit())
                                .salaryPerHour(job.getSalaryPerHour())
                                .scheduleDays(job.getScheduleDays())
                                .scheduleTime(job.getScheduleTime())
                                .status(job.getStatus())
                                .distance(distance)
                                .build();
        }
}
