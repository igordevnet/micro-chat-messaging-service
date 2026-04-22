package com.microservice.microchatmessagingservice.controller.dtos.response;

import com.microservice.microchatmessagingservice.domain.ActionType;

public record MessageDeletedEvent(
        String messageId,
        ActionType action
) {}
