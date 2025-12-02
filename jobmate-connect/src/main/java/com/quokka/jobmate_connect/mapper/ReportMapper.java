package com.quokka.jobmate_connect.mapper;

import com.quokka.jobmate_connect.dto.response.report.ReportResponse;
import com.quokka.jobmate_connect.entity.Report;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ReportMapper {

    @Mapping(source = "reporter.id", target = "reporterId")
    @Mapping(source = "reporter.email", target = "reporterEmail")
    @Mapping(source = "reporter.fullName", target = "reporterFullName")
    @Mapping(target = "jobTitle", ignore = true)
    @Mapping(target = "jobOwnerId", ignore = true)
    @Mapping(target = "jobOwnerEmail", ignore = true)
    @Mapping(target = "jobOwnerFullName", ignore = true)
    @Mapping(target = "reviewedByEmail", ignore = true)
    ReportResponse toReportResponse(Report report);
}
