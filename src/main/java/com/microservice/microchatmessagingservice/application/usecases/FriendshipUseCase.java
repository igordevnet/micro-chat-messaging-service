package com.microservice.microchatmessagingservice.application.usecases;

import com.microservice.microchatmessagingservice.application.exceptions.FriendshipAlreadyExistsException;
import com.microservice.microchatmessagingservice.application.exceptions.UnauthorizedActionException;
import com.microservice.microchatmessagingservice.application.gateways.FriendshipGateway;
import com.microservice.microchatmessagingservice.application.gateways.MessageBrokerGateway;
import com.microservice.microchatmessagingservice.controller.dtos.request.FriendshipAnswerRequest;
import com.microservice.microchatmessagingservice.controller.dtos.request.FriendshipRequest;
import com.microservice.microchatmessagingservice.controller.dtos.response.FriendshipResponse;
import com.microservice.microchatmessagingservice.domain.Friendship;
import com.microservice.microchatmessagingservice.domain.enums.FriendshipStatus;
import com.microservice.microchatmessagingservice.infrastructure.persistence.mappers.FriendshipMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FriendshipUseCase {

    private final FriendshipGateway friendshipGateway;
    private final FriendshipMapper friendshipMapper;
    private final MessageBrokerGateway messageBrokerGateway;

    public void sendFriendshipRequest(FriendshipRequest friendshipRequest, Long requesterId) {

        throwIfFriendshipAlreadyExist(requesterId, friendshipRequest.receiverId());

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

        throwIfUserIsNotTheReceiver(senderId, friendship);

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

        throwIfUserIsNotTheReceiver(senderId, friendship);

        friendship.setStatus(FriendshipStatus.BLOCKED);

        var savedFriendship = friendshipGateway.saveFriendship(friendship);

        var response = friendshipMapper.domainToResponse(savedFriendship);

        Long targetId = friendship.getRequesterId().equals(senderId) ? friendship.getReceiverId() : friendship.getRequesterId();

        sendToBroker(targetId ,response);
    }

    public List<Long> getAcceptedFriendIds(Long userId) {
        return friendshipGateway.getAcceptedFriendIds(userId);
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

    private void throwIfUserIsNotTheReceiver(Long senderId, Friendship friendship) {
        if (!friendship.getReceiverId().equals(senderId)) {
            throw new UnauthorizedActionException("Only the receiver can answer this friend request.");
        }
    }

    private void throwIfFriendshipAlreadyExist(Long requesterId, Long receiverId) {
        var friendship = friendshipGateway.existsByUsers(requesterId, receiverId);

        if (friendship) {
            throw new FriendshipAlreadyExistsException("You can't send the request twice");
        }
    }
}
