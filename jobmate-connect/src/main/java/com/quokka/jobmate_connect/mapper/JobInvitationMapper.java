package com.quokka.jobmate_connect.mapper;

import com.quokka.jobmate_connect.dto.response.invatation.JobInvitationResponse;
import com.quokka.jobmate_connect.entity.JobInvitation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface JobInvitationMapper {
    @Mapping(source = "employer.id", target = "employerId")
    @Mapping(source = "candidate.id", target = "candidateId")
    @Mapping(source = "job.id", target = "jobId")
    @Mapping(source = "waitingList.id", target = "waitingListId")
    @Mapping(source = "job.title", target = "title")
    JobInvitationResponse toInvitation(JobInvitation entity);
}
