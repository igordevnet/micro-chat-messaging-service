package com.microservice.microchatmessagingservice.application.gateways;

import com.microservice.microchatmessagingservice.domain.Friendship;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public interface FriendshipGateway {
    Friendship saveFriendship(Friendship friendship);

    Friendship getFriendshipById(@NotNull UUID uuid);

    List<Long> getAcceptedFriendIds(Long userId);

    Boolean existsByUsers(Long requesterId, Long receiverId);
}
