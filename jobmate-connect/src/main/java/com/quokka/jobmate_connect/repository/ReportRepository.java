package com.quokka.jobmate_connect.repository;

import com.quokka.jobmate_connect.constant.ReportStatus;
import com.quokka.jobmate_connect.entity.Report;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ReportRepository extends JpaRepository<Report, UUID> {
    List<Report> findByTargetId(UUID targetId);
    long countByTargetIdAndStatus(UUID targetId, ReportStatus status);
    Page<Report> findByStatus(ReportStatus status, Pageable pageable);

    long countByStatus(ReportStatus status);

    boolean existsByReporter_IdAndTargetId(UUID reporterId, UUID jobId);
}
