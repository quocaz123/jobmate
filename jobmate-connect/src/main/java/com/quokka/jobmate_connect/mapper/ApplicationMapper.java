package com.quokka.jobmate_connect.mapper;

import com.quokka.jobmate_connect.dto.request.application.ApplicationRequest;
import com.quokka.jobmate_connect.dto.response.application.ApplicationListResponse;
import com.quokka.jobmate_connect.dto.response.application.ApplicationResponse;
import com.quokka.jobmate_connect.dto.response.application.ApplicationDetailResponse;
import com.quokka.jobmate_connect.entity.Application;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = { JobMapper.class, UserMapper.class })
public interface ApplicationMapper {

    Application toApplication(ApplicationRequest request);

    // ✅ full response (detail)
    @Mapping(source = "job.id", target = "jobId")
    @Mapping(source = "job.title", target = "jobTitle")
    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "user", target = "applicant")
    @Mapping(source = "job", target = "job")
    ApplicationResponse toApplicationResponse(Application application);

    // ✅ list view (short)
    @Mapping(source = "id", target = "applicationId")
    @Mapping(source = "status", target = "status")
    @Mapping(source = "appliedAt", target = "appliedAt")
    @Mapping(source = "user.id", target = "applicantId")
    @Mapping(source = "user.fullName", target = "fullName")
    @Mapping(source = "user.avatarUrl", target = "avatarUrl")
    @Mapping(source = "user.address", target = "address")
    @Mapping(source = "user.skills", target = "skills")
    @Mapping(source = "user.preferredJobType", target = "preferredJobType")
    @Mapping(source = "user.trustScore", target = "trustScore")
    @Mapping(source = "job.id", target = "jobId")
    @Mapping(source = "job.title", target = "jobTitle")
    @Mapping(source = "job.companyName", target = "companyName")
    @Mapping(source = "job.location", target = "location")
    @Mapping(source = "job.salary", target = "salary")
    @Mapping(source = "job.salaryUnit", target = "salaryUnit")
    @Mapping(source = "job.workingDays", target = "workingDays")
    @Mapping(source = "job.workingHours", target = "workingHours")
    @Mapping(source = "job.jobType", target = "jobType")
    @Mapping(source = "job.status", target = "statusJob")
    ApplicationListResponse toListResponse(Application app);

    // ✅ detail view
    @Mapping(source = "user.id", target = "applicantId")
    @Mapping(source = "user.fullName", target = "applicantName")
    @Mapping(source = "user.email", target = "email")
    @Mapping(source = "user.contactPhone", target = "contactPhone")
    @Mapping(source = "user.address", target = "address")
    @Mapping(source = "user.skills", target = "skills")
    @Mapping(source = "user.bio", target = "bio")
    @Mapping(source = "user.avatarUrl", target = "avatarUrl")
    @Mapping(source = "job.id", target = "jobId")
    @Mapping(source = "job.title", target = "jobTitle")
    @Mapping(source = "job.companyName", target = "companyName")
    @Mapping(source = "job.salary", target = "salary")
    @Mapping(source = "job.salaryUnit", target = "salaryUnit")
    @Mapping(source = "job.workingDays", target = "workingDays")
    @Mapping(source = "job.workingHours", target = "workingHours")
    ApplicationDetailResponse toDetailResponse(Application app);
}
