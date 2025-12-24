package com.quokka.jobmate_connect.repository;

import com.quokka.jobmate_connect.constant.InvitationStatus;
import com.quokka.jobmate_connect.entity.JobInvitation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface JobInvitationRepository extends JpaRepository<JobInvitation, UUID> {
    List<JobInvitation> findByCandidateId(UUID candidateId);

    List<JobInvitation> findByEmployerId(UUID employerId);

    List<JobInvitation> findByCandidate_IdOrderByCreatedAtDesc(UUID candidateId);

    List<JobInvitation> findByEmployer_IdOrderByCreatedAtDesc(UUID employerId);

    boolean existsByEmployer_IdAndCandidate_IdAndJob_Id(UUID employerId, UUID candidateId, UUID jobId);

    List<JobInvitation> findByCandidate_IdAndStatus(UUID candidateId, InvitationStatus status);

    List<JobInvitation> findByJob_IdAndStatus(UUID jobId, InvitationStatus status);

    List<JobInvitation> findByWaitingList_Id(UUID waitingListId);

    boolean existsByWaitingList_IdAndStatus(UUID waitingListId, InvitationStatus status);

    List<JobInvitation> findByWaitingList_IdAndStatus(UUID waitingListId, InvitationStatus status);
}
