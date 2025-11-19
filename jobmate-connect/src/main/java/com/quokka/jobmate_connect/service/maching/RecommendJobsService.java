package com.quokka.jobmate_connect.service.maching;

import co.elastic.clients.elasticsearch._types.query_dsl.RangeQuery;
import co.elastic.clients.json.JsonData;
import com.quokka.jobmate_connect.dto.PageResponse;
import com.quokka.jobmate_connect.dto.response.job.JobESResponse;
import com.quokka.jobmate_connect.dto.response.waitinglist.WaitingListRecommendResponse;
import com.quokka.jobmate_connect.dto.response.waitinglist.WaitingListResponse;
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

        WaitingList wl = waitingListRepository.findById(waitingListId)
                .orElseThrow(() -> new RuntimeException("Waiting List not found"));

        List<JobESResponse> jobs = recommendByWaitingList(user, wl);

        return PageResponse.<List<JobESResponse>>builder()
                .currentPage(0)
                .totalPages(1)
                .pageSize(jobs.size())
                .totalElements(jobs.size())
                .data(List.of(jobs))
                .build();
    }


    private List<JobESResponse> recommendByWaitingList(User user, WaitingList wl) {

        String jobType = wl.getJobType() != null ? wl.getJobType().name() : null;

        Double minSalary = wl.getExpectedMinSalary() != null
                ? wl.getExpectedMinSalary().doubleValue() : null;

        NativeQuery query = NativeQuery.builder()
                .withQuery(q -> q.bool(b -> {

                    if (jobType != null) {
                        b.must(m -> m.term(t -> t.field("jobType").value(jobType)));
                    }

                    if (wl.getSkills() != null && !wl.getSkills().isBlank()) {
                        b.must(m -> m.match(t -> t
                                .field("skills")
                                .query(wl.getSkills())
                                .fuzziness("AUTO")));
                    }

                    if (minSalary != null && minSalary > 0) {
                        RangeQuery rq = RangeQuery.of(r -> r.untyped(u -> u
                                .field("salary").gte(JsonData.of(minSalary))));
                        b.filter(f -> f.range(rq));
                    }
                    return b;
                }))
                .withPageable(PageRequest.of(0, 200))
                .build();


        var hits = elasticsearchTemplate.search(query, JobES.class);

        List<JobES> rawJobs = hits.getSearchHits().stream()
                .map(SearchHit::getContent)
                .toList();

        // ---- map to dto + calculate distance ----
        List<JobESResponse> filtered = rawJobs.stream()
                .map(job -> {
                    double distance = -1;

                    if (job.getLocation() != null &&
                    user.getLatitude() != null && user.getLongitude() != null) {
                        distance = geocodingService.calculateDistance(
                                user.getLatitude(), user.getLongitude(),
                                job.getLocation().getLat(), job.getLocation().getLon()
                        );
                    }
                    return mapToJobES(job, distance);
                }
                )
                .filter(dto ->
                        wl.getSearchRadius() == null ||
                                wl.getSearchRadius() <= 0 ||
                                dto.getDistance() == -1 ||   // job không có location → vẫn giữ
                                dto.getDistance() <= wl.getSearchRadius()
                )
                .toList();

        // ---- sort theo score ----
        filtered = filtered.stream()
                .sorted((a, b) -> Double.compare(
                        matchingEngine.calculateScoreJobForUser(user, wl, b),
                        matchingEngine.calculateScoreJobForUser(user, wl, a)
                ))
                .limit(20)
                .toList();

        return filtered;
    }

    public List<WaitingListRecommendResponse> recommendWaitingListForJob(UUID jobId) {

        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new AppException(ErrorCode.JOB_NOT_FOUND));

        List<WaitingList> all = waitingListRepository.findAll();

        return all.stream()
                .map(wl -> mapWaitingListToDto(job, wl))
                .filter(dto ->
                        dto.getRadius() == null ||
                                dto.getDistance() == null ||
                                dto.getDistance() <= dto.getRadius()
                )
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
                    job.getLatitude(), job.getLongitude()
            );
        }

        double score = matchingEngine.calculateScoreUserForJob(
                job,
                wl,
                distance == null ? -1 : distance
        );

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
                .scheduleDays(job.getScheduleDays())
                .scheduleTime(job.getScheduleTime())
                .status(job.getStatus())
                .distance(distance)
                .build();
    }
}
