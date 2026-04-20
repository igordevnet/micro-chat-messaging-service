package com.microservice.microchatmessagingservice.controller.dtos.reponse;

import java.time.LocalDateTime;

public record MessageResponse(
        String id,
        Long senderId,
        String content,
        Boolean edited,
        Boolean read,

        LocalDateTime createdAt
) {
}
