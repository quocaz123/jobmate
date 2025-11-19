package com.quokka.jobmate_connect.mapper;

import com.quokka.jobmate_connect.dto.response.file.FileResponse;
import com.quokka.jobmate_connect.dto.response.file.FileResumeResponse;
import com.quokka.jobmate_connect.entity.FileMgmt;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface FileMapper {

    FileResponse toFileMgmtResponse(FileMgmt fileMgmt);

    @Mapping(target = "type", expression = "java(fileMgmt.getType() != null ? fileMgmt.getType().toString() : null)")
    @Mapping(target = "fileName", expression = "java(fileMgmt.getUrl() != null ? fileMgmt.getUrl().substring(fileMgmt.getUrl().lastIndexOf('/') + 1) : null)")
    FileResumeResponse toFileResumeResponse(FileMgmt fileMgmt);

    @AfterMapping
    default void mapFileName(FileMgmt file, @MappingTarget FileResumeResponse response) {
        if (file.getUrl() != null) {
            String url = file.getUrl();
            response.setUrl(url);
            // Extract fileName from URL
            response.setFileName(url.substring(url.lastIndexOf('/') + 1));
        }
    }

}
