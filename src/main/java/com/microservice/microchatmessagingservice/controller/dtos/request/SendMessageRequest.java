package com.microservice.microchatmessagingservice.controller.dtos.request;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record SendMessageRequest(
        @NotNull
        String content,
        @NotNull
        LocalDateTime createdAt
) {
}
