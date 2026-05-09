package com.microservice.microchatmessagingservice;

import com.microservice.microchatmessagingservice.application.exceptions.ChatNotFoundException;
import com.microservice.microchatmessagingservice.application.gateways.ChatGateway;
import com.microservice.microchatmessagingservice.application.usecases.ChatUseCase;
import com.microservice.microchatmessagingservice.controller.dtos.request.ChatRequest;
import com.microservice.microchatmessagingservice.controller.dtos.response.ChatResponse;
import com.microservice.microchatmessagingservice.domain.Chat;
import com.microservice.microchatmessagingservice.domain.ChatParticipant;
import com.microservice.microchatmessagingservice.domain.enums.ChatType;
import com.microservice.microchatmessagingservice.infrastructure.persistence.mappers.ChatMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatUseCaseTest {

    @Mock
    private ChatGateway chatGateway;

    @Mock
    private ChatMapper chatMapper;

    @InjectMocks
    private ChatUseCase chatUseCase;

    private UUID chatId;
    private Long userId;
    private Chat chat;
    private ChatParticipant participant;

    @BeforeEach
    void setUp() {
        chatId = UUID.randomUUID();
        userId = 1L;

        chat = new Chat();
        chat.setId(chatId);

        participant = new ChatParticipant();
        participant.setUserId(userId);
        participant.setChatId(chatId);

        chat.setParticipants(new ArrayList<>(List.of(participant)));
    }

    @Test
    @DisplayName("Should successfully create a chat and add creator as participant")
    void shouldCreateChat() {
        List<Long> initialParticipantIds = new ArrayList<>();
        ChatRequest request = new ChatRequest("Room Name", ChatType.GROUP, initialParticipantIds);

        Chat mappedChat = new Chat();
        mappedChat.setParticipants(new ArrayList<>());

        ChatResponse expectedResponse = new ChatResponse(
                chatId, "Room Name", ChatType.GROUP, null, null, null, null, new ArrayList<>()
        );

        when(chatMapper.requestToDomain(request)).thenReturn(mappedChat);
        when(chatGateway.saveChat(any(Chat.class))).thenReturn(mappedChat);
        when(chatMapper.domainToResponse(mappedChat)).thenReturn(expectedResponse);

        ChatResponse result = chatUseCase.createChat(request, userId);

        assertNotNull(result);
        verify(chatGateway).saveChat(mappedChat);
        assertNotNull(mappedChat.getCreatedAt());
    }

    @Test
    @DisplayName("Should update chat details successfully")
    void shouldUpdateChat() {
        ChatRequest request = new ChatRequest("New Name", ChatType.GROUP, new ArrayList<>());
        Chat mappedChat = new Chat();

        ChatResponse expectedResponse = new ChatResponse(
                chatId, "New Name", ChatType.GROUP, null, null, null, null, new ArrayList<>()
        );

        when(chatMapper.requestToDomain(request)).thenReturn(mappedChat);
        when(chatGateway.saveChat(any(Chat.class))).thenReturn(mappedChat);
        when(chatMapper.domainToResponse(mappedChat)).thenReturn(expectedResponse);

        ChatResponse result = chatUseCase.updateChat(chatId, request);

        assertEquals("New Name", result.chatName());
        assertEquals(chatId, mappedChat.getId());
        assertNotNull(mappedChat.getUpdatedAt());
        verify(chatGateway).saveChat(mappedChat);
    }

    @Test
    @DisplayName("Should delete chat when user is a valid participant")
    void shouldDeleteChatWhenUserHasPermission() {
        when(chatGateway.getChat(chatId)).thenReturn(Optional.of(chat));

        chatUseCase.deleteChat(userId, chatId);

        verify(chatGateway).deleteChat(chatId);
    }

    @Test
    @DisplayName("Should NOT delete chat and log warning when user lacks permission")
    void shouldNotDeleteChatWhenUserLacksPermission() {
        Long hackerId = 99L;

        when(chatGateway.getChat(chatId)).thenReturn(Optional.of(chat));

        chatUseCase.deleteChat(hackerId, chatId);

        verify(chatGateway, never()).deleteChat(any());
    }

    @Test
    @DisplayName("Should silently return when trying to delete a non-existent chat")
    void shouldDoNothingWhenDeletingNonExistentChat() {
        when(chatGateway.getChat(chatId)).thenReturn(Optional.empty());

        chatUseCase.deleteChat(userId, chatId);

        verify(chatGateway, never()).deleteChat(any());
    }

    @Test
    @DisplayName("Should return a list of mapped chat responses for a user")
    void shouldGetAllChatsForUser() {
        List<Chat> chatList = List.of(chat);
        ChatResponse responseMock = new ChatResponse(
                chatId, "Name", ChatType.ONE_ON_ONE, null, null, null, null, new ArrayList<>()
        );
        List<ChatResponse> responseList = List.of(responseMock);

        when(chatGateway.getChatList(userId)).thenReturn(chatList);
        when(chatMapper.domainToResponseList(chatList)).thenReturn(responseList);

        List<ChatResponse> result = chatUseCase.getAllChats(userId);

        assertEquals(1, result.size());
        verify(chatGateway).getChatList(userId);
    }

    @Test
    @DisplayName("Should return Chat domain object by ID")
    void shouldGetChatById() {
        when(chatGateway.getChat(chatId)).thenReturn(Optional.of(chat));

        Chat result = chatUseCase.getChatById(chatId);

        assertNotNull(result);
        assertEquals(chatId, result.getId());
    }

    @Test
    @DisplayName("Should throw ChatNotFoundException when ID is invalid")
    void shouldThrowExceptionWhenChatNotFound() {
        when(chatGateway.getChat(chatId)).thenReturn(Optional.empty());

        assertThrows(ChatNotFoundException.class, () -> chatUseCase.getChatById(chatId));
    }
}