package com.microservice.microchatmessagingservice.controller.dtos.response;

import com.microservice.microchatmessagingservice.domain.enums.Status;

public record UserStatusEvent(
        Long userId,
        Status status
) {
}
