package com.quokka.jobmate_connect.controller;

import com.quokka.jobmate_connect.constant.FileTypeStatus;
import com.quokka.jobmate_connect.dto.ApiResponse;
import com.quokka.jobmate_connect.dto.response.file.FileResponse;
import com.quokka.jobmate_connect.service.FileService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

@RestController
@RequestMapping("/files")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FileController {

    FileService fileService;

    @PostMapping("/upload")
    public ApiResponse<FileResponse> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("type") FileTypeStatus type
    ) throws IOException {

        return ApiResponse.success(fileService.uploadFile(file, type));
    }
}
