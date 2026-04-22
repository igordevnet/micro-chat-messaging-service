package com.microservice.microchatmessagingservice.controller.dtos.response;

import com.microservice.microchatmessagingservice.domain.ActionType;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record ReadReceiptEvent(
        UUID chatId,
        Long userId,
        LocalDateTime time,
        ActionType actionType
) {
}
