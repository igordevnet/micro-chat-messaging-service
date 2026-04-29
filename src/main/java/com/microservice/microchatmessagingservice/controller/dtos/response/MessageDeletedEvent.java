package com.microservice.microchatmessagingservice.controller.dtos.response;

import com.microservice.microchatmessagingservice.domain.enums.ActionType;

import java.util.UUID;

public record MessageDeletedEvent(
        String messageId,
        UUID chatId,
        ActionType action
) {}
