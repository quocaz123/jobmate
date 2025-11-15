package com.quokka.jobmate_connect.mapper;

import com.quokka.jobmate_connect.dto.response.report.ReportResponse;
import com.quokka.jobmate_connect.entity.Report;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ReportMapper {

    // Chuyển Report entity -> ReportResponse DTO
    @Mapping(source = "reporter.fullName", target = "reporterName")
    @Mapping(source = "reporter.email", target = "reporterEmail")
    ReportResponse toReportResponse(Report report);
}
