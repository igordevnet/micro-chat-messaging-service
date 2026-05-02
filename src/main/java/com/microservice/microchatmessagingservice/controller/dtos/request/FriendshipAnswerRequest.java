package com.microservice.microchatmessagingservice.controller.dtos.request;

import com.microservice.microchatmessagingservice.domain.enums.FriendshipStatus;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record FriendshipAnswerRequest(
        @NotNull
        UUID friendshipId,
        FriendshipStatus status
) {
}
