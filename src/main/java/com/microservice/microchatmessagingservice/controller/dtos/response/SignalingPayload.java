package com.microservice.microchatmessagingservice.controller.dtos.response;

import com.microservice.microchatmessagingservice.domain.enums.SignalingType;
import lombok.Builder;

import java.util.UUID;

@Builder
public record SignalingPayload(
        SignalingType type,
        Long senderId,
        Long targetId,
        UUID chatId,
        String data
) {}
