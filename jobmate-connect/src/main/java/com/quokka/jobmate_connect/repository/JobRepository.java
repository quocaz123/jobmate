package com.quokka.jobmate_connect.repository;

import com.quokka.jobmate_connect.constant.JobStatus;
import com.quokka.jobmate_connect.constant.JobType;
import com.quokka.jobmate_connect.entity.Job;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface JobRepository extends JpaRepository<Job, UUID> {

     // Lấy danh sách công việc theo trạng thái
     Page<Job> findByStatus(JobStatus status, Pageable pageable);

     Page<Job> findByCreatedById(UUID userId, Pageable pageable);

     Page<Job> findByCreatedByIdAndStatusNot(UUID userId, JobStatus status, Pageable pageable);

     long countByCreatedById(UUID userId);

     long countByCreatedByIdAndStatus(UUID userId, JobStatus status);

     long countByCreatedByIdAndStatusIn(UUID userId, Collection<JobStatus> statuses);

     long countByStatus(JobStatus status);

     Page<Job> findAllJobsByStatus(JobStatus status, Pageable pageable);

     // Lọc theo người tạo và trạng thái
     Page<Job> findByCreatedByIdAndStatus(UUID userId, JobStatus status, Pageable pageable);

     // Tìm job theo tiêu đề (title) chứa từ khóa, chỉ lấy job đã duyệt
     Page<Job> findByTitleContainingIgnoreCaseAndStatus(String title, JobStatus status, Pageable pageable);

     // Tìm job theo địa điểm (location), chỉ lấy job đã duyệt
     Page<Job> findByLocationContainingIgnoreCaseAndStatus(String location, JobStatus status, Pageable pageable);

     // Tìm job với nhiều bộ lọc tùy chọn, chỉ lấy job đã duyệt
     @Query("""
               SELECT j FROM Job j
               WHERE j.status = :status
                 AND (:keyword IS NULL OR :keyword = '' OR
                      LOWER(j.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
                      LOWER(j.companyName) LIKE LOWER(CONCAT('%', :keyword, '%')))
                 AND (:location IS NULL OR :location = '' OR
                      LOWER(j.location) LIKE LOWER(CONCAT('%', :location, '%')))
                 AND (:jobType IS NULL OR j.jobType = :jobType)
                 AND (:workMode IS NULL OR :workMode = '' OR j.workMode = :workMode)
                 AND (:categoryId IS NULL OR j.category.id = :categoryId)
                 AND (:salaryMin IS NULL OR j.salary >= :salaryMin)
                 AND (:salaryMax IS NULL OR j.salary <= :salaryMax)
               """)
     Page<Job> searchAvailableJobs(
               @Param("status") JobStatus status,
               @Param("keyword") String keyword,
               @Param("location") String location,
               @Param("jobType") JobType jobType,
               @Param("workMode") String workMode,
               @Param("categoryId") UUID categoryId,
               @Param("salaryMin") BigDecimal salaryMin,
               @Param("salaryMax") BigDecimal salaryMax,
               Pageable pageable);

     @EntityGraph(attributePaths = "createdBy")
     List<Job> findByIdIn(List<UUID> ids);

     @EntityGraph(attributePaths = "createdBy")
     java.util.Optional<Job> findById(UUID id);

     // Tìm tất cả job đang hoạt động của một user (APPROVED, PENDING_REVIEW)
     @Query("SELECT j FROM Job j WHERE j.createdBy.id = :userId AND j.status IN :statuses")
     List<Job> findByCreatedByIdAndStatusIn(@Param("userId") UUID userId,
               @Param("statuses") Collection<JobStatus> statuses);
}
