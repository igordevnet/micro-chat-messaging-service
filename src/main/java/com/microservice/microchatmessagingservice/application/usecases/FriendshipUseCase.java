package com.microservice.microchatmessagingservice.application.usecases;

import com.microservice.microchatmessagingservice.application.gateways.FriendshipGateway;
import com.microservice.microchatmessagingservice.application.gateways.MessageBrokerGateway;
import com.microservice.microchatmessagingservice.controller.dtos.request.FriendshipAnswerRequest;
import com.microservice.microchatmessagingservice.controller.dtos.request.FriendshipRequest;
import com.microservice.microchatmessagingservice.controller.dtos.response.FriendshipResponse;
import com.microservice.microchatmessagingservice.domain.Friendship;
import com.microservice.microchatmessagingservice.domain.enums.FriendshipStatus;
import com.microservice.microchatmessagingservice.infrastructure.persistence.mappers.FriendshipMapper;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FriendshipUseCase {

    private final FriendshipGateway friendshipGateway;
    private final FriendshipMapper friendshipMapper;
    private final MessageBrokerGateway messageBrokerGateway;

    public void sendFriendshipRequest(FriendshipRequest friendshipRequest, Long requesterId) {
        Friendship friendship = Friendship.builder()
                .requesterId(requesterId)
                .receiverId(friendshipRequest.receiverId())
                .status(FriendshipStatus.PENDING)
                .build();

        var savedFriendship = friendshipGateway.saveFriendship(friendship);

        var response = friendshipMapper.domainToResponse(savedFriendship);

        sendToBroker(friendship.getReceiverId(), response);
    }

    public void answerFriendshipRequest(FriendshipAnswerRequest friendshipRequest, Long senderId) {
        var friendship = getFriendshipById(friendshipRequest.friendshipId());

        Friendship savedFriendship = switch (friendshipRequest.status()) {
            case ACCEPTED -> {
                friendship.setStatus(FriendshipStatus.ACCEPTED);
                yield friendshipGateway.saveFriendship(friendship);
            }
            case DECLINED -> {
                friendship.setStatus(FriendshipStatus.DECLINED);
                yield friendshipGateway.saveFriendship(friendship);
            }
            default -> null;
        };

        var response = friendshipMapper.domainToResponse(savedFriendship);

        sendToBroker(friendship.getRequesterId(), response);
    }

    public void blockFriendship(UUID friendshipId, Long senderId) {
        var friendship = getFriendshipById(friendshipId);

        friendship.setStatus(FriendshipStatus.BLOCKED);

        var savedFriendship = friendshipGateway.saveFriendship(friendship);

        var response = friendshipMapper.domainToResponse(savedFriendship);

        Long targetId = friendship.getRequesterId().equals(senderId) ? friendship.getReceiverId() : friendship.getRequesterId();

        sendToBroker(targetId ,response);
    }

    private Friendship getFriendshipById(UUID friendshipId) {
        return friendshipGateway.getFriendshipById(friendshipId);
    }

    private void sendToBroker(Long targetUserId, FriendshipResponse  friendshipResponse) {
        messageBrokerGateway.convertAndSend(
                "chat.topic",
                "chat.event." + targetUserId,
                friendshipResponse
        );
    }
}
