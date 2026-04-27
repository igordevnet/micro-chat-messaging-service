package com.microservice.microchatmessagingservice.infrastructure.gateways;

import com.microservice.microchatmessagingservice.application.exceptions.FailedUploadException;
import com.microservice.microchatmessagingservice.application.gateways.FileStorageGateway;
import com.microservice.microchatmessagingservice.domain.Attachment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.UUID;

@Component
@Slf4j
public class MinioFileStorageGateway implements FileStorageGateway {

    private final S3Client s3Client;

    @Value("${minio.bucket-name}")
    private String bucketName;

    @Value("${minio.endpoint}")
    private String storageUrl;

    public MinioFileStorageGateway(S3Client s3Client) {
        this.s3Client = s3Client;
    }

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


            String url = String.format(storageUrl + "/%s/%s", bucketName, key);

            return Attachment.builder()
                    .fileName(file.getOriginalFilename())
                    .contentType(file.getContentType())
                    .size(file.getSize())
                    .url(url)
                    .build();
        } catch (IOException e) {
            throw new FailedUploadException("MinIO Upload Failed: " + e);
        }
    }
}
