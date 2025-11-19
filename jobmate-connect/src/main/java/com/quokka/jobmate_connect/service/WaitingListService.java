package com.quokka.jobmate_connect.service;

import com.quokka.jobmate_connect.constant.RequestStatus;
import com.quokka.jobmate_connect.dto.request.waitinglist.CreateWaitingListRequest;
import com.quokka.jobmate_connect.dto.response.waitinglist.WaitingListResponse;
import com.quokka.jobmate_connect.entity.User;
import com.quokka.jobmate_connect.entity.WaitingList;
import com.quokka.jobmate_connect.exception.AppException;
import com.quokka.jobmate_connect.exception.ErrorCode;
import com.quokka.jobmate_connect.mapper.WaitingListMapper;
import com.quokka.jobmate_connect.repository.UserRepository;
import com.quokka.jobmate_connect.repository.WaitingListRepository;
import com.quokka.jobmate_connect.service.ESService.WaitingListIndexerService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class WaitingListService {
    WaitingListRepository waitingListRepository;
    UserRepository userRepository;
    WaitingListIndexerService indexer;
    WaitingListMapper waitingListMaper;

    public WaitingListResponse create(CreateWaitingListRequest request) {
        Jwt jwt = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UUID userId = UUID.fromString(jwt.getClaimAsString("userId"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        if (waitingListRepository.countActiveByUserId(userId) > 5) {
            throw new RuntimeException("User already has an active waiting list");
        }

        WaitingList wl = WaitingList.builder()
                .user(user) // Sử dụng user đã load đầy đủ
                .jobType(request.getJobType())
                .skills(request.getSkills())
                .expectedMinSalary(request.getExpectedMinSalary())
                .latitude(user.getLatitude())
                .longitude(user.getLongitude())
                .searchRadius(request.getSearchRadius())
                .availableDays(request.getAvailableDays())
                .availableTime(request.getAvailableTime())
                .note(request.getNote())
                .status(RequestStatus.PENDING)
                .build();

        waitingListRepository.save(wl);
//        // Reload để đảm bảo user được load đầy đủ trước khi index
//        WaitingList savedWl = waitingListRepository.findById(wl.getId())
//                .orElseThrow(() -> new AppException(ErrorCode.INTERNAL_ERROR));
//        indexer.index(savedWl);

        return waitingListMaper.toWaitingListResponse(wl);
    }

    public List<WaitingListResponse> getMyWaitingList() {
        Jwt jwt = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UUID userId = UUID.fromString(jwt.getClaimAsString("userId"));

        return waitingListRepository.findByUserId(userId).stream()
                .map(waitingListMaper::toWaitingListResponse)
                .toList();
    }

    public void close(UUID id) {
        Jwt jwt = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UUID userId = UUID.fromString(jwt.getClaimAsString("userId"));
        WaitingList wl = waitingListRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Waiting list not found"));

        if (!wl.getUser().getId().equals(userId))
            throw new RuntimeException("Bạn không có quyền");

        wl.setStatus(RequestStatus.CLOSED);
        waitingListRepository.save(wl);
        // Reload để đảm bảo user được load đầy đủ trước khi index
        WaitingList updatedWl = waitingListRepository.findById(wl.getId())
                .orElseThrow(() -> new AppException(ErrorCode.INTERNAL_ERROR));
        indexer.index(updatedWl);
    }


    public List<WaitingListResponse> getActiveCandidates(String jobType, String skills, BigDecimal minSalary) {
        List<WaitingList> waitingLists = waitingListRepository.findActiveCandidates(jobType, skills, minSalary);
        return waitingLists.stream()
                .map(waitingListMaper::toWaitingListResponse)
                .toList();
    }
}
