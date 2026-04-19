package com.microservice.microchatmessagingservice.controller.dtos.request;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record EditMessageRequest(
        @NotNull
        String id,
        @NotNull
        String content,
        @NotNull
        LocalDateTime timestamp
) {
}
