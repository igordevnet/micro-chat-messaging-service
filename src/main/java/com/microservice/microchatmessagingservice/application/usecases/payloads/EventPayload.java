package com.microservice.microchatmessagingservice.application.usecases.payloads;

import lombok.Builder;

import java.util.UUID;

@Builder
public record EventPayload(
        Long senderId,
        Long receiverId,
        UUID chatId,
        String type,
        String content
) {
}
