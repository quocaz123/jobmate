package com.quokka.jobmate_connect.service.ESService;

import com.quokka.jobmate_connect.entity.Job;
import com.quokka.jobmate_connect.entity.eslasticsearch.JobES;
import com.quokka.jobmate_connect.repository.ESRepository.JobESRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;
import org.springframework.stereotype.Service;

import java.time.ZoneId;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class JobIndexerService {
    JobESRepository jobESRepository;

    public void index(Job job) {
        JobES doc = JobES.builder()
                .id(job.getId().toString())
                .employerId(job.getCreatedBy().getId().toString())
                .title(job.getTitle())
                .description(job.getDescription())
                .skills(job.getSkills())
                .jobType(job.getJobType() != null ? job.getJobType().name() : null)
                .salary(job.getSalary() != null ? job.getSalary().doubleValue() : null)
                .salaryUnit(job.getSalaryUnit().name())
                .location((job.getLatitude() != null && job.getLongitude() != null
                        && job.getLatitude() != 0.0 && job.getLongitude() != 0.0)
                                ? new GeoPoint(job.getLatitude(), job.getLongitude())
                                : null)
                .scheduleDays(job.getWorkingDays())
                .scheduleTime(job.getWorkingHours())
                .status(job.getStatus().toString())
                .createdAt(job.getCreatedAt())
                .build();

        jobESRepository.save(doc);
    }
}
