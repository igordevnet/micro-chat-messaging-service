package com.microservice.microchatmessagingservice.application.gateways;

import com.microservice.microchatmessagingservice.domain.Attachment;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public interface FileStorageGateway {
    Attachment store(MultipartFile file, UUID chatId);
}
