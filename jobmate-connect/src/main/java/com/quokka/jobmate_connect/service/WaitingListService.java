package com.quokka.jobmate_connect.service;

import com.quokka.jobmate_connect.constant.RequestStatus;
import com.quokka.jobmate_connect.dto.PageResponse;
import com.quokka.jobmate_connect.dto.request.waitinglist.CreateWaitingListRequest;
import com.quokka.jobmate_connect.dto.response.waitinglist.WaitingListResponse;
import com.quokka.jobmate_connect.entity.User;
import com.quokka.jobmate_connect.entity.WaitingList;
import com.quokka.jobmate_connect.exception.AppException;
import com.quokka.jobmate_connect.exception.ErrorCode;
import com.quokka.jobmate_connect.mapper.WaitingListMapper;
import com.quokka.jobmate_connect.constant.InvitationStatus;
import com.quokka.jobmate_connect.repository.JobInvitationRepository;
import com.quokka.jobmate_connect.repository.UserRepository;
import com.quokka.jobmate_connect.repository.WaitingListRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class WaitingListService {
    WaitingListRepository waitingListRepository;
    UserRepository userRepository;
    WaitingListMapper waitingListMapper;
    JobInvitationRepository jobInvitationRepository;

    public WaitingListResponse create(CreateWaitingListRequest request) {
        Jwt jwt = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UUID userId = UUID.fromString(jwt.getClaimAsString("userId"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        if (waitingListRepository.countActiveByUserId(userId) > 5) {
            throw new AppException(ErrorCode.USER_ALREADY_HAS_ACTIVE_WAITING_LIST);
        }

        WaitingList wl = WaitingList.builder()
                .user(user)
                .jobType(request.getJobType())
                .skills(request.getSkills())
                .expectedMinSalary(request.getExpectedMinSalary())
                .expectedSalaryUnit(request.getExpectedSalaryUnit())
                .latitude(user.getLatitude())
                .longitude(user.getLongitude())
                .searchRadius(request.getSearchRadius())
                .availableDays(request.getAvailableDays())
                .availableTime(request.getAvailableTime())
                .note(request.getNote())
                .status(RequestStatus.PENDING)
                .build();

        waitingListRepository.save(wl);
        return waitingListMapper.toWaitingListResponse(wl);
    }

    public List<WaitingListResponse> getMyWaitingList() {
        Jwt jwt = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UUID userId = UUID.fromString(jwt.getClaimAsString("userId"));

        return waitingListRepository.findByUserId(userId).stream()
                .map(waitingListMapper::toWaitingListResponse)
                .toList();
    }

    public void close(UUID id) {
        Jwt jwt = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UUID userId = UUID.fromString(jwt.getClaimAsString("userId"));
        WaitingList wl = waitingListRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.WAITING_LIST_NOT_FOUND));

        if (!wl.getUser().getId().equals(userId))
            throw new AppException(ErrorCode.UNAUTHORIZED);

        // Convert tất cả invitation PENDING của waiting list này thành EXPIRED
        var pendingInvitations = jobInvitationRepository.findByWaitingList_IdAndStatus(id, InvitationStatus.PENDING);
        pendingInvitations.forEach(inv -> inv.setStatus(InvitationStatus.EXPIRED));
        if (!pendingInvitations.isEmpty()) {
            jobInvitationRepository.saveAll(pendingInvitations);
        }

        // Soft delete: đổi trạng thái waiting list sang CLOSED (không xóa DB)
        wl.setStatus(RequestStatus.CLOSED);
        waitingListRepository.save(wl);
    }

    public PageResponse<WaitingListResponse> getActiveCandidates(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<WaitingList> waitingLists = waitingListRepository.findAllByStatus(RequestStatus.PENDING, pageable);

        List<WaitingListResponse> responses = waitingLists.getContent().stream()
                .map(waitingListMapper::toWaitingListResponse)
                .toList();

        return PageResponse.<WaitingListResponse>builder()
                .currentPage(waitingLists.getNumber())
                .totalPages(waitingLists.getTotalPages())
                .pageSize(waitingLists.getSize())
                .totalElements(waitingLists.getTotalElements())
                .data(responses)
                .build();
    }
}
