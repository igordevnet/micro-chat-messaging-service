package com.microservice.microchatmessagingservice.application.usecases;

import com.microservice.microchatmessagingservice.application.exceptions.FriendshipAlreadyExistsException;
import com.microservice.microchatmessagingservice.application.exceptions.UnauthorizedActionException;
import com.microservice.microchatmessagingservice.application.gateways.FriendshipGateway;
import com.microservice.microchatmessagingservice.application.gateways.MessageBrokerGateway;
import com.microservice.microchatmessagingservice.controller.dtos.request.FriendshipAnswerRequest;
import com.microservice.microchatmessagingservice.controller.dtos.request.FriendshipRequest;
import com.microservice.microchatmessagingservice.controller.dtos.response.FriendshipResponse;
import com.microservice.microchatmessagingservice.domain.ChatParticipant;
import com.microservice.microchatmessagingservice.domain.Friendship;
import com.microservice.microchatmessagingservice.domain.enums.FriendshipStatus;
import com.microservice.microchatmessagingservice.infrastructure.config.UserAuthenticated;
import com.microservice.microchatmessagingservice.infrastructure.persistence.mappers.FriendshipMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FriendshipUseCase {

    private final FriendshipGateway friendshipGateway;
    private final FriendshipMapper friendshipMapper;
    private final MessageBrokerGateway messageBrokerGateway;
    private final EventUseCase eventUseCase;

    @Transactional
    public void sendFriendshipRequest(FriendshipRequest friendshipRequest, UserAuthenticated user) {

        throwIfFriendshipAlreadyExist(user.id(), friendshipRequest.receiverId());

        Friendship friendship = Friendship.builder()
                .requesterId(user.id())
                .receiverId(friendshipRequest.receiverId())
                .status(FriendshipStatus.PENDING)
                .build();

        var savedFriendship = friendshipGateway.saveFriendship(friendship);

        var response = friendshipMapper.domainToResponse(savedFriendship);

        sendToBroker(friendship.getReceiverId(), response);
        publishEvent(user, friendship.getReceiverId(), FriendshipStatus.PENDING);
    }

    @Transactional
    public void answerFriendshipRequest(FriendshipAnswerRequest friendshipRequest, UserAuthenticated user) {
        var friendship = getFriendshipById(friendshipRequest.friendshipId());

        throwIfUserIsNotTheReceiver(user.id(), friendship);

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

        publishEvent(user, friendship.getRequesterId(), friendship.getStatus());
    }

    @Transactional
    public void blockFriendship(Long userId, Long senderId) {
        var friendship = getFriendsByUsersId(userId, senderId);

        friendship.setStatus(FriendshipStatus.BLOCKED);
        friendship.setBlockedBy(senderId);

        var savedFriendship = friendshipGateway.saveFriendship(friendship);

        var response = friendshipMapper.domainToResponse(savedFriendship);

        Long targetId = friendship.getRequesterId().equals(senderId) ? friendship.getReceiverId() : friendship.getRequesterId();

        sendToBroker(targetId ,response);
    }

    @Transactional
    public void unblockFriendship(Long userId, Long senderId) {
        var friendship = getFriendsByUsersId(userId, senderId);

        friendship.setStatus(FriendshipStatus.ACCEPTED);
        friendship.setBlockedBy(null);

        var savedFriendship = friendshipGateway.saveFriendship(friendship);

        var response = friendshipMapper.domainToResponse(savedFriendship);

        Long targetId = friendship.getRequesterId().equals(senderId) ? friendship.getReceiverId() : friendship.getRequesterId();

        sendToBroker(targetId ,response);
    }

    public List<Long> getAcceptedFriendIds(Long userId) {
        return friendshipGateway.getAcceptedFriendIds(userId);
    }


    public List<FriendshipResponse> getPendingFriend(Long userId) {
        return friendshipGateway.getPendingFriend(userId)
                .stream()
                .map(friendshipMapper::domainToResponse)
                .toList();
    }

    public List<FriendshipResponse> getBlockedFriend(Long userId) {
        return friendshipGateway.getBlockedFriend(userId)
                .stream()
                .map(friendshipMapper::domainToResponse)
                .toList();
    }

    public Friendship getFriendsByUsersId(Long firstId, Long secondId) {
        return friendshipGateway.getFriendshipByUsersId(firstId, secondId);
    }

    public boolean isFriendshipBlocked(Long firstId, Long secondId) {
        var friendship = getFriendsByUsersId(firstId, secondId);

        return friendship.getStatus() == FriendshipStatus.BLOCKED;
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

    private void publishEvent(UserAuthenticated user, Long targetUserId, FriendshipStatus friendshipStatus) {
        switch (friendshipStatus) {
            case PENDING -> eventUseCase.publishEvent(user.id(), targetUserId, null,"REQUEST", user.username() + " sent you a friend request!");
            case ACCEPTED -> eventUseCase.publishEvent(user.id(), targetUserId, null,"ACCEPTED", user.username() + " accepted your friend request!");
        }
    }
}
