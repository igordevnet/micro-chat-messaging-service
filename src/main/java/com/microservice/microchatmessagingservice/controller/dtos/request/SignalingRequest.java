package com.microservice.microchatmessagingservice.controller.dtos.request;

import com.microservice.microchatmessagingservice.domain.enums.SignalingType;

import java.util.UUID;

public record SignalingRequest(
        SignalingType type,
        Long targetId,
        UUID chatId,
        String data
) {}
