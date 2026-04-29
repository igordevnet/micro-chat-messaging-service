package com.microservice.microchatmessagingservice.application.gateways;

import com.microservice.microchatmessagingservice.domain.Friendship;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public interface FriendshipGateway {
    Friendship saveFriendship(Friendship friendship);

    Friendship getFriendshipById(@NotNull UUID uuid);
}
