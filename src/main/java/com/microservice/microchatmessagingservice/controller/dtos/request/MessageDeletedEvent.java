package com.microservice.microchatmessagingservice.controller.dtos.request;

public record MessageDeletedEvent(
        String messageId,
        String action
) {}
