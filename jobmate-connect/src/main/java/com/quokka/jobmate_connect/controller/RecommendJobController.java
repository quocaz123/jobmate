package com.quokka.jobmate_connect.controller;

import com.quokka.jobmate_connect.dto.ApiResponse;
import com.quokka.jobmate_connect.dto.PageResponse;
import com.quokka.jobmate_connect.dto.response.job.JobESResponse;
import com.quokka.jobmate_connect.dto.response.waitinglist.WaitingListRecommendResponse;
import com.quokka.jobmate_connect.service.maching.RecommendJobsService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/recommend")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RecommendJobController {
    RecommendJobsService recommendJobsService;

    @GetMapping("/jobs")
    public ApiResponse<PageResponse<List<JobESResponse>>> recommendJobs(@RequestParam(required = false)
                                                                    UUID waitingListId) {
        return ApiResponse.success(recommendJobsService.recommend(waitingListId));
    }

    @GetMapping("/users")
    public ApiResponse<List<WaitingListRecommendResponse>> recommenUser(@RequestParam UUID jobId) {
        return ApiResponse.success(recommendJobsService.recommendWaitingListForJob(jobId));
    }
}
