package com.microservice.microchatmessagingservice.controller.dtos.response;

import com.microservice.microchatmessagingservice.domain.enums.FriendshipStatus;

import java.util.UUID;

public record FriendshipResponse(
        UUID id,
        Long requesterId,
        Long receiverId,
        FriendshipStatus status
) {
}
