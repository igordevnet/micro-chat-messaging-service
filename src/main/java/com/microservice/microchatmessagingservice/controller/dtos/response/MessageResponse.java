package com.microservice.microchatmessagingservice.controller.dtos.response;

import com.microservice.microchatmessagingservice.domain.MessageType;

import java.time.LocalDateTime;

public record MessageResponse(
        String id,
        Long senderId,
        String content,
        Boolean edited,
        Boolean read,
        MessageType messageType,

        LocalDateTime createdAt
) {
}
