package com.quokka.jobmate_connect.mapper;

import com.quokka.jobmate_connect.dto.response.file.FileResponse;
import com.quokka.jobmate_connect.entity.FileMgmt;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface FileMapper {

    FileResponse toFileMgmtResponse(FileMgmt fileMgmt);
}
