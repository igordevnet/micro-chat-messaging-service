package com.microservice.microchatmessagingservice.controller.dtos.response;

import com.microservice.microchatmessagingservice.domain.Attachment;
import com.microservice.microchatmessagingservice.domain.enums.ActionType;

import java.time.LocalDateTime;
import java.util.UUID;

public record MessageResponse(
        String id,
        UUID chatId,
        Long senderId,
        String content,
        Boolean edited,
        ActionType actionType,
        AttachmentResponse attachment,

        LocalDateTime createdAt
) {
}
