package com.microservice.microchatmessagingservice.application.usecases.payloads;

import lombok.Builder;

@Builder
public record EventPayload(
        Long senderId,
        Long receiverId,
        String type,
        String content
) {
}
