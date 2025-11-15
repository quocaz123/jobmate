package com.quokka.jobmate_connect.repository;

import com.quokka.jobmate_connect.constant.WaitingListStatus;
import com.quokka.jobmate_connect.entity.WaitingList;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WaitingListRepository extends JpaRepository<WaitingList, UUID> {
    
    // Tìm waiting list theo user
    List<WaitingList> findByUserId(UUID userId);
    
    // Tìm waiting list theo status
    List<WaitingList> findByStatus(WaitingListStatus status);
    
    // Tìm waiting list theo user và status
    List<WaitingList> findByUserIdAndStatus(UUID userId, WaitingListStatus status);
    
    // Tìm waiting list đang chờ match (WAITING status)
    @Query("SELECT wl FROM WaitingList wl WHERE wl.status = 'WAITING' ORDER BY wl.createdAt ASC")
    List<WaitingList> findWaitingForMatching(Pageable pageable);
    
    // Tìm waiting list đã match với job cụ thể
    Optional<WaitingList> findByJobId(UUID jobId);
    
    // Tìm waiting list theo user và job
    Optional<WaitingList> findByUserIdAndJobId(UUID userId, UUID jobId);
    
    // Tìm waiting list theo preferred job type và location
    @Query("SELECT wl FROM WaitingList wl WHERE wl.status = 'WAITING' " +
           "AND (:jobType IS NULL OR wl.preferredJobType = :jobType) " +
           "AND (:location IS NULL OR wl.preferredLocation LIKE %:location%)")
    List<WaitingList> findMatchingWaitingList(
            @Param("jobType") String jobType,
            @Param("location") String location,
            Pageable pageable
    );
    
    // Đếm số lượng waiting list theo status
    long countByStatus(WaitingListStatus status);
}


