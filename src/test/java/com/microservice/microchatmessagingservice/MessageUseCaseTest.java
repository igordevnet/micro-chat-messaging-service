package com.microservice.microchatmessagingservice;

import com.microservice.microchatmessagingservice.application.exceptions.UnauthorizedActionException;
import com.microservice.microchatmessagingservice.application.gateways.ChatGateway;
import com.microservice.microchatmessagingservice.application.gateways.MessageGateway;
import com.microservice.microchatmessagingservice.application.usecases.MessageUseCase;
import com.microservice.microchatmessagingservice.controller.dtos.response.MessageResponse;
import com.microservice.microchatmessagingservice.controller.dtos.request.EditMessageRequest;
import com.microservice.microchatmessagingservice.controller.dtos.request.SendMessageRequest;
import com.microservice.microchatmessagingservice.domain.Message;
import com.microservice.microchatmessagingservice.domain.MessageType;
import com.microservice.microchatmessagingservice.infrastructure.persistence.mappers.MessageMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MessageUseCaseTest {

    @Mock private MessageGateway messageGateway;
    @Mock private ChatGateway chatGateway;
    @Mock private MessageMapper messageMapper;

    @InjectMocks
    private MessageUseCase messageUseCase;

    private final UUID chatId = UUID.randomUUID();

    @Test
    @DisplayName("Should throw error when user deletes another person's message")
    void shouldThrowErrorWhenUserDeletesAnotherPersonsMessage() {
        String messageId = "msg-123";
        Long hackerUserId = 99L;

        Message messageFromDb = new Message();
        messageFromDb.setSenderId(1L);
        messageFromDb.setChatId(chatId);

        when(messageGateway.findMessageById(messageId)).thenReturn(Optional.of(messageFromDb));

        assertThrows(UnauthorizedActionException.class,
                () -> messageUseCase.deleteMessage(chatId, messageId, hackerUserId));

        verify(messageGateway, never()).deleteMessage(anyString());
    }

    @Test
    @DisplayName("Should return paginated response by chatId")
    void shouldReturnMessagesByChatId() {
        int page = 0;
        int size = 10;
        Pageable pageable = PageRequest.of(page, size);

        Page<Message> messageFromDbPage = new PageImpl<>(new ArrayList<>());

        when(messageGateway.getMessagePage(eq(chatId), eq(pageable))).thenReturn(messageFromDbPage);

        var result = messageUseCase.getMessages(chatId, page, size);

        assertNotNull(result);
        assertEquals(0, result.content().size());
        verify(messageGateway).getMessagePage(chatId, pageable);
    }

    @Test
    @DisplayName("Should save a message and update chat preview")
    void shouldSaveMessage() {
        Long userId = 99L;
        String contentText = "content";
        SendMessageRequest request = new SendMessageRequest(contentText, LocalDateTime.now());

        Message domainMessage = new Message();
        Message savedMessage = new Message();
        savedMessage.setContent(contentText);
        savedMessage.setCreatedAt(LocalDateTime.now());

        MessageResponse expectedResponse = new MessageResponse("msg-123", userId, contentText, false, false, MessageType.NEW_MESSAGE, LocalDateTime.now());

        when(messageMapper.sendRequestToDomain(request)).thenReturn(domainMessage);
        when(messageGateway.saveMessage(any(Message.class))).thenReturn(savedMessage);
        when(messageMapper.domainToResponse(savedMessage)).thenReturn(expectedResponse);

        messageUseCase.saveMessage(chatId, userId, request);

        verify(chatGateway).updateLastMessage(eq(chatId), anyString(), any());
        verify(messageGateway).saveMessage(domainMessage);
    }

    @Test
    @DisplayName("Should edit a message after validating ownership")
    void shouldEditMessage() {
        String messageId = "msg-123";
        Long userId = 99L;
        EditMessageRequest request = new EditMessageRequest(messageId, "new content");

        Message existingMessage = new Message();
        existingMessage.setId(messageId);
        existingMessage.setSenderId(userId);

        when(messageGateway.findMessageById(messageId)).thenReturn(Optional.of(existingMessage));
        when(messageGateway.updateMessage(any(Message.class))).thenReturn(existingMessage);

        messageUseCase.editMessage(chatId, userId, request);

        assertEquals("new content", existingMessage.getContent());
        assertTrue(existingMessage.getEdited());
        verify(messageGateway).updateMessage(existingMessage);
    }

    @Test
    @DisplayName("Should delete message and recalculate preview (Telegram Style)")
    void shouldDeleteMessage() {
        String messageId = "msg-123";
        Long userId = 99L;

        Message messageFromDb = new Message();
        messageFromDb.setSenderId(userId);
        messageFromDb.setChatId(chatId);

        Message previousMessage = new Message();
        previousMessage.setContent("mensagem anterior");

        when(messageGateway.findMessageById(messageId)).thenReturn(Optional.of(messageFromDb));
        when(messageGateway.findLastMessageByChatId(chatId)).thenReturn(Optional.of(previousMessage));

        messageUseCase.deleteMessage(chatId, messageId, userId);

        verify(messageGateway).deleteMessage(messageId);
        verify(chatGateway).forceUpdateLastMessagePreview(eq(chatId), eq("mensagem anterior"), any());
    }
}