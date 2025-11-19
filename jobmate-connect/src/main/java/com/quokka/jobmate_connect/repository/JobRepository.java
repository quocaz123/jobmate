package com.quokka.jobmate_connect.repository;

import com.quokka.jobmate_connect.constant.JobStatus;
import com.quokka.jobmate_connect.entity.Job;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface JobRepository extends JpaRepository<Job, UUID> {

    // Lấy danh sách công việc theo trạng thái
    Page<Job> findByStatus(JobStatus status, Pageable pageable);

    Page<Job> findByCreatedById(UUID userId, Pageable pageable);

    Page<Job> findAllJobsByStatus(JobStatus status, Pageable pageable);

    // Lọc theo người tạo và trạng thái
    Page<Job> findByCreatedByIdAndStatus(UUID userId, JobStatus status, Pageable pageable);

    // Tìm job theo tiêu đề (title) chứa từ khóa, chỉ lấy job đã duyệt
    Page<Job> findByTitleContainingIgnoreCaseAndStatus(String title, JobStatus status, Pageable pageable);

    // Tìm job theo địa điểm (location), chỉ lấy job đã duyệt
    Page<Job> findByLocationContainingIgnoreCaseAndStatus(String location, JobStatus status, Pageable pageable);
}
