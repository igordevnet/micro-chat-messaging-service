package com.microservice.microchatmessagingservice.controller.dtos;

import java.time.LocalDateTime;

public record NotificationEventDto(
        Long senderId,
        Long receiverId,
        String type,
        String content,
        LocalDateTime timestamp
) {}
