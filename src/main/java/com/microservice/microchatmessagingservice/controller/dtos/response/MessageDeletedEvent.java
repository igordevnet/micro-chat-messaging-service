package com.microservice.microchatmessagingservice.controller.dtos.response;

import com.microservice.microchatmessagingservice.domain.MessageType;

public record MessageDeletedEvent(
        String messageId,
        MessageType action
) {}
