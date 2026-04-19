package com.microservice.microchatmessagingservice.controller.dtos.request;

import com.microservice.microchatmessagingservice.domain.ChatType;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record ChatRequest(
        String chatName,
        ChatType type,

        @NotNull
        List<Long> participants
) {
}
