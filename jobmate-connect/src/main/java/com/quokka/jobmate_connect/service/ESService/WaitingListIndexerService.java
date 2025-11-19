package com.quokka.jobmate_connect.service.ESService;

import com.quokka.jobmate_connect.entity.WaitingList;
import com.quokka.jobmate_connect.entity.eslasticsearch.WaitingRequestES;
import com.quokka.jobmate_connect.repository.ESRepository.WaitingRequestESRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;
import org.springframework.stereotype.Service;

import java.time.ZoneId;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class WaitingListIndexerService {
        WaitingRequestESRepository waitingRequestESRepository;

        public void index(WaitingList request) {
                WaitingRequestES doc = WaitingRequestES.builder()
                                .id(request.getId().toString())
                                .userId(request.getUser().getId().toString())
                                .jobType(request.getJobType() != null ? request.getJobType().name() : null)
                                .skills(request.getSkills())
                                .expectedMinSalary(
                                                request.getExpectedMinSalary() != null
                                                                ? request.getExpectedMinSalary().doubleValue()
                                                                : null)
                                .expectedSalaryUnit(request.getExpectedSalaryUnit() != null
                                                ? request.getExpectedSalaryUnit().name()
                                                : null)
                                .location(new GeoPoint(request.getUser().getLatitude(),
                                                request.getUser().getLongitude()))
                                .searchRadius(request.getSearchRadius())
                                .availableDays(request.getAvailableDays())
                                .availableTime(request.getAvailableTime())
                                .note(request.getNote())
                                .status(request.getStatus())
                                .createdAt(request.getCreatedAt() != null
                                                ? request.getCreatedAt().atZone(ZoneId.systemDefault()).toInstant()
                                                                .toEpochMilli()
                                                : null)
                                .build();

                waitingRequestESRepository.save(doc);
        }
}
