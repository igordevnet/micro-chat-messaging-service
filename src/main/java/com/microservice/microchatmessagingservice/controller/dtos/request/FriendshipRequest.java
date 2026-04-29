package com.microservice.microchatmessagingservice.controller.dtos.request;

import com.microservice.microchatmessagingservice.domain.enums.FriendshipStatus;
import jakarta.validation.constraints.NotNull;

public record FriendshipRequest(
        @NotNull
        Long receiverId,
        FriendshipStatus status
) {
}
