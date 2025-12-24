package com.quokka.jobmate_connect.service;

import com.quokka.jobmate_connect.constant.FileTypeStatus;
import com.quokka.jobmate_connect.dto.response.file.FileResponse;
import com.quokka.jobmate_connect.entity.FileMgmt;
import com.quokka.jobmate_connect.exception.AppException;
import com.quokka.jobmate_connect.exception.ErrorCode;
import com.quokka.jobmate_connect.mapper.FileMapper;
import com.quokka.jobmate_connect.repository.FileMgtRepository;
import com.quokka.jobmate_connect.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FileService {
    FileMgtRepository fileMgtRepository;
    FileMapper fileMapper;
    S3Service s3Service;
    private final UserRepository userRepository;

    @Transactional
    public FileResponse uploadFile(MultipartFile file, FileTypeStatus type) throws IOException {
        var auth = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UUID userId = UUID.fromString(auth.getClaim("userId"));

        boolean isPrivate = (type == FileTypeStatus.CCCD_FRONT || type == FileTypeStatus.CCCD_BACK);

        Set<String> ALLOWED_TYPES = Set.of(
                "image/png",
                "image/jpg",
                "image/jpeg",
                "application/pdf",
                "application/msword",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        if (!ALLOWED_TYPES.contains(file.getContentType())) {
            throw new AppException(ErrorCode.FILE_TYPE_NOT_ALLOWED);
        }

        // 1) Upload file mới lên S3
        String s3Key = s3Service.uploadFile(file, type, isPrivate);
        String publicUrl = isPrivate ? null : s3Service.getPublicUrl(s3Key);

        // 2) Tìm bản ghi hiện có
        FileMgmt fileMgmt = fileMgtRepository.findByOwnerIdAndType(userId, type).orElse(null);

        if (fileMgmt != null) {
            // 2.1) Xóa S3 cũ (nếu khác key)
            if (fileMgmt.getS3Key() != null && !fileMgmt.getS3Key().equals(s3Key)) {
                s3Service.deleteFile(fileMgmt.getS3Key());
            }
            // 2.2) Cập nhật bản ghi
            fileMgmt.setS3Key(s3Key);
            fileMgmt.setUrl(publicUrl);
            fileMgmt.setContentType(file.getContentType());
            fileMgmt.setSize(file.getSize());
            fileMgmt.setCreatedAt(LocalDateTime.now());
        } else {
            // 3) Tạo mới
            fileMgmt = FileMgmt.builder()
                    .ownerId(userId)
                    .type(type)
                    .s3Key(s3Key)
                    .url(publicUrl)
                    .contentType(file.getContentType())
                    .size(file.getSize())
                    .createdAt(LocalDateTime.now())
                    .build();
        }

        fileMgtRepository.save(fileMgmt);

        if (type == FileTypeStatus.AVATAR && !isPrivate) {
            String avatarUrl = publicUrl;
            userRepository.findById(userId).ifPresent(user -> {
                user.setAvatarUrl(avatarUrl);
                userRepository.save(user);
            });
        }

        return fileMapper.toFileMgmtResponse(fileMgmt);
    }

    public String getPrivateFileUrl(FileTypeStatus type, UUID userId) {

        int expireMinutes = switch (type) {
            case CCCD_FRONT, CCCD_BACK -> 5;
            case RESUME -> 15;
            default -> 10;
        };

        FileMgmt file = fileMgtRepository.findByOwnerIdAndType(userId, type)
                .orElseThrow(() -> new AppException(ErrorCode.FILE_NOT_FOUND));
        return s3Service.generatePresignedUrl(file.getS3Key(), expireMinutes);
    }
}
