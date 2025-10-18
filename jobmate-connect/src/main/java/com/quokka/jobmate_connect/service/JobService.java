package com.quokka.jobmate_connect.service;

import com.quokka.jobmate_connect.constant.JobStatus;
import com.quokka.jobmate_connect.dto.PageResponse;
import com.quokka.jobmate_connect.dto.request.job.JobCreationRequest;
import com.quokka.jobmate_connect.dto.response.job.JobResponse;
import com.quokka.jobmate_connect.entity.Job;
import com.quokka.jobmate_connect.entity.User;
import com.quokka.jobmate_connect.exception.AppException;
import com.quokka.jobmate_connect.exception.ErrorCode;
import com.quokka.jobmate_connect.mapper.JobMapper;
import com.quokka.jobmate_connect.repository.JobRepository;
import com.quokka.jobmate_connect.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class JobService {
    JobRepository jobRepository;
    JobMapper jobMapper;
    GeocodingService geocodingService;

    static final double EARTH_RADIUS_KM = 6371.0;
    private final UserRepository userRepository;

    public JobResponse createJob(JobCreationRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Jwt jwt = (Jwt) auth.getPrincipal();
        UUID userId = UUID.fromString(jwt.getClaim("userId"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        double[] coordinates = geocodingService.getCoordinates(request.getLocation());

        Job job = Job.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .location(request.getLocation())
                .latitude(coordinates[0])
                .longitude(coordinates[1])
                .salary(request.getSalary())
                .jobType(request.getJobType())
                .skills(request.getSkills())
                .status(JobStatus.OPEN)
                .createdBy(user)
                .startAt(request.getStartAt())
                .deadline(request.getDeadline())
                .createdAt(LocalDateTime.now())
                .build();

        jobRepository.save(job);

        return jobMapper.toJobResponse(job);
    }

    public PageResponse<JobResponse> getAllJobs(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Job> jobPage = jobRepository.findAll(pageable);

        return PageResponse.<JobResponse>builder()
                .currentPage(page + 1)
                .totalPages(jobPage.getTotalPages())
                .pageSize(size)
                .totalElements(jobPage.getTotalElements())
                .data(jobPage.map(jobMapper::toJobResponse).getContent())
                .build();
    }

    public PageResponse<JobResponse> getNearByJob(double radiusKm, int page, int size) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Jwt jwt = (Jwt) auth.getPrincipal();
        UUID userId = UUID.fromString(jwt.getClaim("userId"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        Pageable pageable = PageRequest.of(page, size);
        Page<Job> jobPage = jobRepository.findByStatus(JobStatus.OPEN, pageable);

        List<JobResponse> nearby = jobPage.stream()
                .map(job -> {
                    double distance = geocodingService.calculateDistance(
                            user.getLatitude(), user.getLongitude(),
                            job.getLatitude(), job.getLongitude());

                    return JobResponse.builder()
                            .id(job.getId())
                            .title(job.getTitle())
                            .description(job.getDescription())
                            .location(job.getLocation())
                            .latitude(job.getLatitude())
                            .longitude(job.getLongitude())
                            .salary(job.getSalary())
                            .jobType(job.getJobType())
                            .skills(job.getSkills())
                            .status(job.getStatus())
                            .createdByName(job.getCreatedBy().getFullName())
                            .createdAt(job.getCreatedAt())
                            .deadline(job.getDeadline())
                            .distance(distance)
                            .build();
                })
                .filter(j -> j.getDistance() <= radiusKm)
                .sorted(Comparator.comparing(JobResponse::getDistance))
                .toList();

        return PageResponse.<JobResponse>builder()
                .currentPage(page + 1)
                .pageSize(size)
                .totalPages(jobPage.getTotalPages())
                .totalElements(nearby.size())
                .data(nearby)
                .build();
    }

    public void approveJob(UUID jobId) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found"));

        job.setStatus(JobStatus.APPROVED);
        job.setUpdatedAt(LocalDateTime.now());
        jobRepository.save(job);
    }
}
