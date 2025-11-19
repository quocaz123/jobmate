package com.quokka.jobmate_connect.controller;

import com.quokka.jobmate_connect.dto.ApiResponse;
import com.quokka.jobmate_connect.dto.PageResponse;
import com.quokka.jobmate_connect.entity.eslasticsearch.JobES;
import com.quokka.jobmate_connect.entity.eslasticsearch.WaitingRequestES;
import com.quokka.jobmate_connect.repository.ESRepository.JobESRepository;
import com.quokka.jobmate_connect.repository.ESRepository.WaitingRequestESRepository;
import com.quokka.jobmate_connect.repository.JobRepository;
import com.quokka.jobmate_connect.repository.WaitingListRepository;
import com.quokka.jobmate_connect.service.ESService.JobIndexerService;
import com.quokka.jobmate_connect.service.ESService.WaitingListIndexerService;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/elasticsearch")
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class ElasticsearchController {

    JobESRepository jobESRepository;
    WaitingRequestESRepository waitingRequestESRepository;
    JobRepository jobRepository;
    JobIndexerService jobIndexerService;
    WaitingListIndexerService waitingListIndexerService;
    WaitingListRepository watingListRepository;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/jobs")
    public ApiResponse<PageResponse<List<JobES>>> getAllJobsInES(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);

        Page<JobES> jobPage = jobESRepository.findAll(pageable);

        PageResponse<List<JobES>> response = PageResponse.<List<JobES>>builder()
                .currentPage(jobPage.getNumber())
                .totalPages(jobPage.getTotalPages())
                .pageSize(jobPage.getSize())
                .totalElements(jobPage.getTotalElements())
                .data(jobPage.getContent().stream().map(List::of).toList())
                .build();

        return ApiResponse.success(response);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/jobs/{id}")
    public ApiResponse<JobES> getJobById(@PathVariable String id) {
        return ApiResponse.success(jobESRepository.findById(id)
                .orElse(null));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/waiting-requests")
    public ApiResponse<Page<WaitingRequestES>> getAllWaitingRequestsInES(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ApiResponse.success(waitingRequestESRepository.findAll(pageable));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/waiting-requests/{id}")
    public ApiResponse<WaitingRequestES> getWaitingRequestById(@PathVariable String id) {
        return ApiResponse.success(waitingRequestESRepository.findById(id)
                .orElse(null));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/jobs/count")
    public ApiResponse<Long> getJobsCount() {
        return ApiResponse.success(jobESRepository.count());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/waiting-requests/count")
    public ApiResponse<Long> getWaitingRequestsCount() {
        return ApiResponse.success(waitingRequestESRepository.count());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/jobs/reindex")
    public ApiResponse<String> reindexAllJobs() {
        long count = jobRepository.count();
        jobRepository.findAll().forEach(jobIndexerService::index);
        return ApiResponse.success("Đã re-index " + count + " jobs vào Elasticsearch");
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/waiting-requests/reindex")
    public ApiResponse<String> reindexAllWaitingList() {
        long count = watingListRepository.count();
        watingListRepository.findAll().forEach(waitingListIndexerService::index);
        return ApiResponse.success("Đã re-index " + count + " waitingList vào Elasticsearch");
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/jobs")
    public ApiResponse<String> deleteAllJobs() {
        long count = jobESRepository.count();
        jobESRepository.deleteAll();
        return ApiResponse.success("Đã xóa " + count + " jobs khỏi Elasticsearch");
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/jobs/{id}")
    public ApiResponse<String> deleteJobById(@PathVariable String id) {
        jobESRepository.deleteById(id);
        return ApiResponse.success("Đã xóa job với id: " + id);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/waiting-requests")
    public ApiResponse<String> deleteAllWaitingRequests() {
        long count = waitingRequestESRepository.count();
        waitingRequestESRepository.deleteAll();
        return ApiResponse.success("Đã xóa " + count + " waiting requests khỏi Elasticsearch");
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/waiting-requests/{id}")
    public ApiResponse<String> deleteWaitingRequestById(@PathVariable String id) {
        waitingRequestESRepository.deleteById(id);
        return ApiResponse.success("Đã xóa waiting request với id: " + id);
    }
}
