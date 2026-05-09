package com.microservice.microchatmessagingservice;

import com.microservice.microchatmessagingservice.application.exceptions.FriendshipAlreadyExistsException;
import com.microservice.microchatmessagingservice.application.exceptions.UnauthorizedActionException;
import com.microservice.microchatmessagingservice.application.gateways.FriendshipGateway;
import com.microservice.microchatmessagingservice.application.gateways.MessageBrokerGateway;
import com.microservice.microchatmessagingservice.application.usecases.FriendshipUseCase;
import com.microservice.microchatmessagingservice.controller.dtos.request.FriendshipAnswerRequest;
import com.microservice.microchatmessagingservice.controller.dtos.request.FriendshipRequest;
import com.microservice.microchatmessagingservice.controller.dtos.response.FriendshipResponse;
import com.microservice.microchatmessagingservice.domain.Friendship;
import com.microservice.microchatmessagingservice.domain.enums.FriendshipStatus;
import com.microservice.microchatmessagingservice.infrastructure.persistence.mappers.FriendshipMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FriendshipUseCaseTest {

    @Mock private FriendshipGateway friendshipGateway;
    @Mock private FriendshipMapper friendshipMapper;
    @Mock private MessageBrokerGateway messageBrokerGateway;

    @InjectMocks
    private FriendshipUseCase friendshipUseCase;

    @Captor
    private ArgumentCaptor<Friendship> friendshipCaptor;

    private Long requesterId;
    private Long receiverId;
    private UUID friendshipId;
    private Friendship existingFriendship;
    private FriendshipResponse mockResponse;

    @BeforeEach
    void setUp() {
        requesterId = 100L;
        receiverId = 200L;
        friendshipId = UUID.randomUUID();

        existingFriendship = Friendship.builder()
                .id(friendshipId)
                .requesterId(requesterId)
                .receiverId(receiverId)
                .status(FriendshipStatus.PENDING)
                .build();

        mockResponse = new FriendshipResponse(friendshipId, requesterId, receiverId, FriendshipStatus.PENDING);
    }

    @Test
    @DisplayName("Should successfully send a friendship request and route to broker")
    void shouldSendFriendshipRequest() {
        FriendshipRequest request = new FriendshipRequest(receiverId, FriendshipStatus.PENDING);

        when(friendshipGateway.existsByUsers(requesterId, receiverId)).thenReturn(false);
        when(friendshipGateway.saveFriendship(any(Friendship.class))).thenAnswer(i -> i.getArguments()[0]);
        when(friendshipMapper.domainToResponse(any(Friendship.class))).thenReturn(mockResponse);

        friendshipUseCase.sendFriendshipRequest(request, requesterId);

        verify(friendshipGateway).saveFriendship(friendshipCaptor.capture());
        assertEquals(FriendshipStatus.PENDING, friendshipCaptor.getValue().getStatus());

        verify(messageBrokerGateway).convertAndSend(eq("chat.topic"), eq("chat.event." + receiverId), eq(mockResponse));
    }

    @Test
    @DisplayName("Should throw exception if friendship request already exists")
    void shouldThrowWhenFriendshipExists() {
        FriendshipRequest request = new FriendshipRequest(receiverId, FriendshipStatus.PENDING);
        when(friendshipGateway.existsByUsers(requesterId, receiverId)).thenReturn(true);

        assertThrows(FriendshipAlreadyExistsException.class, () -> friendshipUseCase.sendFriendshipRequest(request, requesterId));
    }

    @Test
    @DisplayName("Should allow RECEIVER to ACCEPT a friendship request")
    void shouldAcceptFriendshipRequest() {
        FriendshipAnswerRequest request = new FriendshipAnswerRequest(friendshipId, FriendshipStatus.ACCEPTED);

        when(friendshipGateway.getFriendshipById(friendshipId)).thenReturn(existingFriendship);
        when(friendshipGateway.saveFriendship(any(Friendship.class))).thenAnswer(i -> i.getArguments()[0]);
        when(friendshipMapper.domainToResponse(any(Friendship.class))).thenReturn(mockResponse);

        friendshipUseCase.answerFriendshipRequest(request, receiverId);

        verify(friendshipGateway).saveFriendship(friendshipCaptor.capture());
        assertEquals(FriendshipStatus.ACCEPTED, friendshipCaptor.getValue().getStatus());
    }

    @Test
    @DisplayName("Should allow RECEIVER to block the friendship and notify requester")
    void shouldAllowReceiverToBlockFriendship() {
        when(friendshipGateway.getFriendshipById(friendshipId)).thenReturn(existingFriendship);
        when(friendshipGateway.saveFriendship(any(Friendship.class))).thenAnswer(i -> i.getArguments()[0]);
        when(friendshipMapper.domainToResponse(any(Friendship.class))).thenReturn(mockResponse);

        friendshipUseCase.blockFriendship(friendshipId, receiverId);

        verify(friendshipGateway).saveFriendship(friendshipCaptor.capture());
        assertEquals(FriendshipStatus.BLOCKED, friendshipCaptor.getValue().getStatus());
        verify(messageBrokerGateway).convertAndSend(eq("chat.topic"), eq("chat.event." + requesterId), eq(mockResponse));
    }

    @Test
    @DisplayName("Should allow REQUESTER to block the friendship and notify receiver")
    void shouldAllowRequesterToBlockFriendship() {
        when(friendshipGateway.getFriendshipById(friendshipId)).thenReturn(existingFriendship);
        when(friendshipGateway.saveFriendship(any(Friendship.class))).thenAnswer(i -> i.getArguments()[0]);
        when(friendshipMapper.domainToResponse(any(Friendship.class))).thenReturn(mockResponse);

        friendshipUseCase.blockFriendship(friendshipId, requesterId);

        verify(friendshipGateway).saveFriendship(friendshipCaptor.capture());
        assertEquals(FriendshipStatus.BLOCKED, friendshipCaptor.getValue().getStatus());
        verify(messageBrokerGateway).convertAndSend(eq("chat.topic"), eq("chat.event." + receiverId), eq(mockResponse));
    }

    @Test
    @DisplayName("Should throw exception if a 3rd party hacker tries to block the friendship")
    void shouldThrowIfOutsiderTriesToBlock() {
        Long hackerId = 999L;
        when(friendshipGateway.getFriendshipById(friendshipId)).thenReturn(existingFriendship);

        assertThrows(UnauthorizedActionException.class, () -> friendshipUseCase.blockFriendship(friendshipId, hackerId));
        verify(friendshipGateway, never()).saveFriendship(any());
    }
}