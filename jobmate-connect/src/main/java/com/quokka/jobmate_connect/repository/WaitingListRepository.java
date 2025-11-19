package com.quokka.jobmate_connect.repository;

import com.quokka.jobmate_connect.entity.WaitingList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface WaitingListRepository extends JpaRepository<WaitingList, UUID> {

    @Query("SELECT COUNT(w) FROM WaitingList w WHERE w.user.id = :userId AND w.status != 'CLOSED'")
    int countActiveByUserId(UUID userId);

    @Query("SELECT w FROM WaitingList w JOIN FETCH w.user WHERE w.user.id = :userId")
    List<WaitingList> findByUserId(UUID userId);

    // Tìm waiting list active (cho employer tìm ứng viên)
    @Query("SELECT w FROM WaitingList w JOIN FETCH w.user WHERE w.status = 'PENDING' " +
            "AND (:jobType IS NULL OR w.jobType = :jobType) " +
            "AND (:skills IS NULL OR LOWER(w.skills) LIKE LOWER(CONCAT('%', :skills, '%'))) " +
            "AND (:minSalary IS NULL OR w.expectedMinSalary <= :minSalary)")
    List<WaitingList> findActiveCandidates(String jobType, String skills, java.math.BigDecimal minSalary);
}
