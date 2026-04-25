package com.microservice.microchatmessagingservice.controller.dtos.response;

import com.microservice.microchatmessagingservice.domain.ActionType;

import java.time.LocalDateTime;
import java.util.UUID;

public record MessageResponse(
        String id,
        UUID chatId,
        Long senderId,
        String content,
        Boolean edited,
        ActionType actionType,

        LocalDateTime createdAt
) {
}
