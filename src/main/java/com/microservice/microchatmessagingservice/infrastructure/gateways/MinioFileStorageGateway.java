package com.microservice.microchatmessagingservice.infrastructure.gateways;

import com.microservice.microchatmessagingservice.application.exceptions.FailedDeleteException;
import com.microservice.microchatmessagingservice.application.exceptions.FailedUploadException;
import com.microservice.microchatmessagingservice.application.gateways.FileStorageGateway;
import com.microservice.microchatmessagingservice.domain.Attachment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.io.IOException;
import java.time.Duration;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class MinioFileStorageGateway implements FileStorageGateway {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    @Value("${minio.bucket-name}")
    private String bucketName;

    @Value("${minio.endpoint}")
    private String storageUrl;

    @Override
    public Attachment store(MultipartFile file, UUID chatId) {
        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        String key = chatId + "/" + fileName;

        try {
            s3Client.putObject(PutObjectRequest.builder()
                            .bucket(bucketName)
                            .key(key)
                            .contentType(file.getContentType())
                            .build(),
                    RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

            return Attachment.builder()
                    .fileName(file.getOriginalFilename())
                    .contentType(file.getContentType())
                    .size(file.getSize())
                    .key(key)
                    .build();
        } catch (IOException e) {
            throw new FailedUploadException("MinIO Upload Failed: " + e);
        }
    }

    @Override
    public void delete(String key) {
        try {
            s3Client.deleteObject(DeleteObjectRequest.builder().bucket(bucketName).key(key).build());
        } catch (SdkClientException e) {
            throw new FailedDeleteException("MinIO Delete Failed:" + e);
        }
    }

    @Override
    public String generatePresignedUrl(String key) {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(15))
                .getObjectRequest(getObjectRequest)
                .build();

        PresignedGetObjectRequest presignedRequest = s3Presigner.presignGetObject(presignRequest);

        return presignedRequest.url().toString();
    }
}
