package com.quokka.jobmate_connect.controller;

import com.quokka.jobmate_connect.dto.ApiResponse;
import com.quokka.jobmate_connect.dto.PageResponse;
import com.quokka.jobmate_connect.entity.eslasticsearch.JobES;
import com.quokka.jobmate_connect.repository.ESRepository.JobESRepository;
import com.quokka.jobmate_connect.repository.JobRepository;
import com.quokka.jobmate_connect.repository.WaitingListRepository;
import com.quokka.jobmate_connect.service.ESService.JobIndexerService;
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
    JobRepository jobRepository;
    JobIndexerService jobIndexerService;
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
    @GetMapping("/jobs/count")
    public ApiResponse<Long> getJobsCount() {
        return ApiResponse.success(jobESRepository.count());
    }


    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/jobs/reindex")
    public ApiResponse<String> reindexAllJobs() {
        long count = jobRepository.count();
        jobRepository.findAll().forEach(jobIndexerService::index);
        return ApiResponse.success("Đã re-index " + count + " jobs vào Elasticsearch");
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

}
