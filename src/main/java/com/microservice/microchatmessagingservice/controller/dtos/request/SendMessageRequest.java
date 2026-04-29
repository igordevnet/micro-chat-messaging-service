package com.microservice.microchatmessagingservice.controller.dtos.request;

import com.microservice.microchatmessagingservice.domain.enums.MessageType;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record SendMessageRequest(
        @NotNull
        String content,
        @NotNull
        LocalDateTime createdAt,
        @NotNull
        MessageType messageType
) {
}
