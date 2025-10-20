package com.quokka.jobmate_connect.service;

import com.quokka.jobmate_connect.constant.FileTypeStatus;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class S3Service {
    final S3Client s3Client;

    @NonFinal
    @Value("${aws.s3.bucket-name}")
    String bucketName;

    @NonFinal
    @Value("${aws.s3.base-url}")
    String baseUrl;

    @NonFinal
    @Value("${aws.s3.region}")
    String region;

    @NonFinal
    @Value("${aws.s3.access-key}")
    String accessKey;

    @NonFinal
    @Value("${aws.s3.secret-key}")
    String secretKey;

    public String uploadFile(MultipartFile file, FileTypeStatus type, boolean isPrivate) throws IOException {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        Jwt jwt = (Jwt) auth.getPrincipal();
        UUID userId = UUID.fromString(jwt.getClaim("userId"));

        String key = String.format(
                "users/%s/%s/%s",
                userId,
                type.name().toLowerCase(),
                file.getOriginalFilename().replaceAll("\\s+", "_"));

        try {
            PutObjectRequest.Builder putBuilder = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType(file.getContentType());


            s3Client.putObject(putBuilder.build(),
                    RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
        } catch (S3Exception e) {
            throw new RuntimeException("Upload to S3 failed: " + e.awsErrorDetails().errorMessage(), e);
        }
        return key;
    }

    public String getPublicUrl(String key) {
        return String.format("%s/%s", baseUrl, key);
    }

    public void deleteFile(String key) {
        try {
            s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build());
        } catch (S3Exception e) {
            throw new RuntimeException("Delete from S3 failed: " + e.awsErrorDetails().errorMessage(), e);
        }
    }

    public byte[] downloadFile(String key) throws IOException {
        try {
            var response = s3Client.getObject(GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build());
            return response.readAllBytes();
        } catch (S3Exception e) {
            throw new RuntimeException("Download from S3 failed: " + e.awsErrorDetails().errorMessage(), e);
        }
    }

    public String generatePresignedUrl(String key, int expireMinutes) {
        var presigner = S3Presigner.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKey, secretKey)))
                .build();

        var request = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();

        var presignedRequest = presigner.presignGetObject(builder -> builder.getObjectRequest(request)
                .signatureDuration(java.time.Duration.ofMinutes(expireMinutes)));

        return presignedRequest.url().toString();
    }
}
