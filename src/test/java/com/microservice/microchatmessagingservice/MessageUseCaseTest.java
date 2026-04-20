package com.microservice.microchatmessagingservice;

import com.microservice.microchatmessagingservice.application.exceptions.UnauthorizedActionException;
import com.microservice.microchatmessagingservice.application.gateways.MessageGateway;
import com.microservice.microchatmessagingservice.application.usecases.MessageUseCase;
import com.microservice.microchatmessagingservice.controller.dtos.response.MessageResponse;
import com.microservice.microchatmessagingservice.controller.dtos.request.EditMessageRequest;
import com.microservice.microchatmessagingservice.controller.dtos.request.SendMessageRequest;
import com.microservice.microchatmessagingservice.domain.Message;
import com.microservice.microchatmessagingservice.infrastructure.persistence.mappers.MessageMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MessageUseCaseTest {

    @Mock
    private MessageGateway messageGateway;

    @Mock
    private MessageMapper messageMapper;

    @InjectMocks
    private MessageUseCase messageUseCase;

    @Test
    @DisplayName("Should throw error when user deletes another person's message")
    void shouldThrowErrorWhenUserDeletesAnotherPersonsMessage() {
        String messageId = "msg-123";
        Long hackerUserId = 99L;

        Message messageFromDb = new Message();
        messageFromDb.setSenderId(1L);

        when(messageGateway.findMessageById(messageId)).thenReturn(Optional.of(messageFromDb));

        UnauthorizedActionException exception = assertThrows(
                UnauthorizedActionException.class,
                () -> messageUseCase.deleteMessage(chatId, messageId, hackerUserId)
        );

        assertEquals("You can't delete this message", exception.getMessage());

        verify(messageGateway, never()).deleteMessage(anyString());
    }

    @Test
    @DisplayName("Should return messages by chatId")
    void shouldReturnMessagesByChatId() {
        UUID chatId = UUID.randomUUID();
        int  page = 0;
        int size = 10;

        Page<Message> messageFromDbPage = new PageImpl<>(new ArrayList<>());

        when(messageGateway.getMessagePage(eq(chatId), any(Pageable.class)))
                .thenReturn(messageFromDbPage);

        Page<MessageResponse> messageResponses = messageUseCase.getMessages(chatId, page, size);

        assertNotNull(messageResponses);
        assertEquals(0, messageResponses.getTotalElements());

        verify(messageGateway, times(1)).getMessagePage(eq(chatId), any(Pageable.class));
    }

    @Test
    @DisplayName("Should save a message")
    void shouldSaveMessage() {
        UUID chatId = UUID.randomUUID();
        Long userId = 99L;
        String contentText = "content";
        LocalDateTime now = LocalDateTime.now();

        SendMessageRequest request = new SendMessageRequest(contentText, now);

        Message domainMessage = new Message();
        Message savedMessage = new Message();

        MessageResponse expectedResponse = new MessageResponse(
                "msg-123",
                userId,
                contentText,
                false,
                false,
                now
        );

        when(messageMapper.sendRequestToDomain(request)).thenReturn(domainMessage);

        when(messageGateway.saveMessage(any(Message.class))).thenReturn(savedMessage);

        when(messageMapper.domainToResponse(savedMessage)).thenReturn(expectedResponse);

        MessageResponse result = messageUseCase.saveMessage(chatId, userId, request);

        assertNotNull(result);

        assertEquals(expectedResponse, result);

        verify(messageMapper, times(1)).sendRequestToDomain(request);
        verify(messageGateway, times(1)).saveMessage(domainMessage);
        verify(messageMapper, times(1)).domainToResponse(savedMessage);
    }

    @Test
    @DisplayName("Should edit a message")
    void shouldEditMessage() {
        UUID chatId = UUID.randomUUID();
        String messageId = "msg-123";
        Long userId = 99L;
        String contentText = "content";
        LocalDateTime now = LocalDateTime.now();

        EditMessageRequest request = new EditMessageRequest(messageId ,contentText, now);

        Message domainMessage = new Message();
        Message editedMessage = new Message();

        MessageResponse expectedResponse = new MessageResponse(
                messageId,
                userId,
                contentText,
                false,
                false,
                now
        );

        when(messageMapper.editRequestToDomain(request)).thenReturn(domainMessage);

        when(messageGateway.updateMessage(any(Message.class))).thenReturn(editedMessage);

        when(messageMapper.domainToResponse(editedMessage)).thenReturn(expectedResponse);

        MessageResponse result = messageUseCase.editMessage(chatId, userId, request);

        assertNotNull(result);

        assertEquals(expectedResponse, result);

        verify(messageMapper, times(1)).editRequestToDomain(request);
        verify(messageGateway, times(1)).updateMessage(domainMessage);
        verify(messageMapper, times(1)).domainToResponse(editedMessage);
    }

    @Test
    @DisplayName("Should delete a message")
    void shouldDeleteMessage() {
        String messageId = "msg-123";
        Long userId = 99L;

        Message messageFromDb = new Message();
        messageFromDb.setSenderId(99L);

        when(messageGateway.findMessageById(messageId)).thenReturn(Optional.of(messageFromDb));

        messageUseCase.deleteMessage(chatId, messageId, userId);

        verify(messageGateway, times(1)).deleteMessage(messageId);
    }
}
