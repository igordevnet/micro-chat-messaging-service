package com.microservice.microchatmessagingservice.controller.dtos.response;

import com.microservice.microchatmessagingservice.domain.ChatType;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record ChatResponse(
        UUID id,
        String chatName,
        ChatType type,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime lastMessageAt,
        String lastMessagePreview,
        List<Long> participants
) {
}
