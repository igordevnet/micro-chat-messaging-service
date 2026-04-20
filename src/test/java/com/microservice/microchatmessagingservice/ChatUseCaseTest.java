package com.microservice.microchatmessagingservice;

import com.microservice.microchatmessagingservice.application.gateways.ChatGateway;
import com.microservice.microchatmessagingservice.application.usecases.ChatUseCase;
import com.microservice.microchatmessagingservice.controller.dtos.response.ChatResponse;
import com.microservice.microchatmessagingservice.controller.dtos.request.ChatRequest;
import com.microservice.microchatmessagingservice.domain.Chat;
import com.microservice.microchatmessagingservice.domain.ChatType;
import com.microservice.microchatmessagingservice.infrastructure.persistence.mappers.ChatMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ChatUseCaseTest {

    @Mock
    private ChatGateway chatGateway;

    @Mock
    private ChatMapper chatMapper;

    @InjectMocks
    private ChatUseCase chatUseCase;

    @Test
    @DisplayName("Should create a new chat")
    void createNewChat() {
        String chatName = "test";
        ChatType chatType = ChatType.ONE_ON_ONE;
        List<Long> participantsIds = List.of(1L, 2L);

        ChatRequest chatRequest = new ChatRequest(chatName, chatType, participantsIds);
        Chat domainChat = new Chat();
        Chat savedChat = new Chat();

        ChatResponse expectedResponse = new ChatResponse(
                UUID.randomUUID(),
                chatName,
                chatType,
                LocalDateTime.now(),
                LocalDateTime.now(),
                null,
                null,
                participantsIds
        );

        when(chatMapper.requestToDomain(chatRequest)).thenReturn(domainChat);
        when(chatGateway.saveChat(domainChat)).thenReturn(savedChat);
        when(chatMapper.domainToResponse(savedChat)).thenReturn(expectedResponse);

        ChatResponse result = chatUseCase.createChat(chatRequest);

        assertNotNull(result);
        assertEquals(expectedResponse, result);

        verify(chatMapper, times(1)).requestToDomain(chatRequest);
        verify(chatGateway, times(1)).saveChat(domainChat);
        verify(chatMapper, times(1)).domainToResponse(savedChat);
    }

    @Test
    @DisplayName("Should update a chat")
    void updateChat() {
        UUID chatId = UUID.randomUUID();
        String chatName = "new_name";
        ChatType chatType = ChatType.GROUP;
        List<Long> participantsIds = List.of(1L, 2L, 3L);

        ChatRequest chatRequest = new ChatRequest(chatName, chatType, participantsIds);
        Chat domainChat = new Chat();
        domainChat.setUpdatedAt(LocalDateTime.now());
        domainChat.setId(chatId);

        Chat editedChat = new Chat();

        ChatResponse expectedResponse = new ChatResponse(
                UUID.randomUUID(),
                chatName,
                chatType,
                LocalDateTime.now(),
                LocalDateTime.now(),
                null,
                null,
                participantsIds
        );

        when(chatMapper.requestToDomain(chatRequest)).thenReturn(domainChat);
        when(chatGateway.saveChat(domainChat)).thenReturn(editedChat);
        when(chatMapper.domainToResponse(editedChat)).thenReturn(expectedResponse);

        ChatResponse result = chatUseCase.updateChat(chatId ,chatRequest);

        assertNotNull(result);
        assertEquals(expectedResponse, result);

        verify(chatMapper, times(1)).requestToDomain(chatRequest);
        verify(chatGateway, times(1)).saveChat(domainChat);
        verify(chatMapper, times(1)).domainToResponse(editedChat);
    }

    @Test
    @DisplayName("Should get all gets by userId")
    void getChatByUserId() {
        Long userId = 1L;

        List<Chat> chats = List.of(new Chat());
        List<ChatResponse> expectedResponse = List.of();

        when(chatGateway.getChatList(userId)).thenReturn(chats);
        when(chatMapper.domainToResponseList(chats)).thenReturn(expectedResponse);

        List<ChatResponse> result = chatUseCase.getAllChats(userId);

        assertNotNull(result);
        assertEquals(expectedResponse, result);
        verify(chatMapper, times(1)).domainToResponseList(chats);
        verify(chatGateway, times(1)).getChatList(userId);
    }

    @Test
    @DisplayName("Should delete a chat by chatId")
    void deleteChatByChatId() {
        UUID chatId = UUID.randomUUID();
        Long userId = 1L;
        List<Long> participantIds = List.of(1L, 2L);

        Chat chatFromDb = new Chat();
        chatFromDb.setParticipants(participantIds);

        when(chatGateway.getChat(chatId)).thenReturn(Optional.of(chatFromDb));

        chatUseCase.deleteChat(userId, chatId);

        verify(chatGateway, times(1)).getChat(chatId);
    }
}
