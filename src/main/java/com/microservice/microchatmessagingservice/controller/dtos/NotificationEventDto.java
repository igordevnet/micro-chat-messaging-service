package com.microservice.microchatmessagingservice.controller.dtos;

import java.time.LocalDateTime;
import java.util.UUID;

public record NotificationEventDto(
        Long senderId,
        Long receiverId,
        String type,
        String content,
        UUID chatId,
        LocalDateTime timestamp
) {}
