package com.quokka.jobmate_connect.repository;

import com.quokka.jobmate_connect.constant.RequestStatus;
import com.quokka.jobmate_connect.entity.WaitingList;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface WaitingListRepository extends JpaRepository<WaitingList, UUID> {

    @Query("SELECT COUNT(w) FROM WaitingList w WHERE w.user.id = :userId AND w.status != 'CLOSED'")
    int countActiveByUserId(UUID userId);

    @Query("SELECT w FROM WaitingList w JOIN FETCH w.user WHERE w.user.id = :userId AND w.status != 'CLOSED'")
    List<WaitingList> findByUserId(UUID userId);

    @Query("SELECT w FROM WaitingList w JOIN FETCH w.user WHERE w.status = :status")
    Page<WaitingList> findAllByStatus(@Param("status") RequestStatus status, Pageable pageable);
}
