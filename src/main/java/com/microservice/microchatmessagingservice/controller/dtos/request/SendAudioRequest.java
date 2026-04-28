package com.microservice.microchatmessagingservice.controller.dtos.request;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record SendAudioRequest(
        @NotNull
        LocalDateTime createdAt,
        @NotNull
        Double duration
) {
}
