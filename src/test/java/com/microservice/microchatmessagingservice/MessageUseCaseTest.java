package com.microservice.microchatmessagingservice;

import com.microservice.microchatmessagingservice.application.exceptions.UnauthorizedActionException;
import com.microservice.microchatmessagingservice.application.gateways.*;
import com.microservice.microchatmessagingservice.application.usecases.ChatUseCase;
import com.microservice.microchatmessagingservice.application.usecases.EventUseCase;
import com.microservice.microchatmessagingservice.application.usecases.MessageUseCase;
import com.microservice.microchatmessagingservice.controller.dtos.request.EditMessageRequest;
import com.microservice.microchatmessagingservice.controller.dtos.request.SendAudioRequest;
import com.microservice.microchatmessagingservice.controller.dtos.request.SendMessageRequest;
import com.microservice.microchatmessagingservice.controller.dtos.response.MessageDeletedEvent;
import com.microservice.microchatmessagingservice.controller.dtos.response.MessagePaginatedResponse;
import com.microservice.microchatmessagingservice.controller.dtos.response.MessageResponse;
import com.microservice.microchatmessagingservice.controller.dtos.response.ReadReceiptEvent;
import com.microservice.microchatmessagingservice.domain.Attachment;
import com.microservice.microchatmessagingservice.domain.Chat;
import com.microservice.microchatmessagingservice.domain.ChatParticipant;
import com.microservice.microchatmessagingservice.domain.Message;
import com.microservice.microchatmessagingservice.domain.enums.ActionType;
import com.microservice.microchatmessagingservice.domain.enums.MessageType;
import com.microservice.microchatmessagingservice.infrastructure.config.UserAuthenticated;
import com.microservice.microchatmessagingservice.infrastructure.persistence.mappers.MessageMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MessageUseCaseTest {

    @Mock private MessageGateway messageGateway;
    @Mock private ChatParticipantGateway chatParticipantGateway;
    @Mock private ChatGateway chatGateway;
    @Mock private MessageMapper messageMapper;
    @Mock private MessageBrokerGateway messageBrokerGateway;
    @Mock private FileStorageGateway fileStorageGateway;
    @Mock private EventUseCase eventUseCase;
    @Mock private ChatUseCase chatUseCase;
    @Mock private MultipartFile mockFile;

    @InjectMocks
    private MessageUseCase messageUseCase;

    @Captor private ArgumentCaptor<Message> messageCaptor;
    @Captor private ArgumentCaptor<Object> brokerPayloadCaptor;

    private UUID chatId;
    private UserAuthenticated authUser;
    private final Long senderId = 1L;
    private final Long receiverId = 2L;
    private Chat mockChat;

    @BeforeEach
    void setUp() {
        chatId = UUID.randomUUID();
        authUser = new UserAuthenticated(senderId, "Igor", "ROLE_USER");

        mockChat = new Chat();
        mockChat.setId(chatId);
        ChatParticipant p1 = new ChatParticipant();
        p1.setUserId(senderId);
        ChatParticipant p2 = new ChatParticipant();
        p2.setUserId(receiverId);
        mockChat.setParticipants(List.of(p1, p2));
    }

    @Test
    @DisplayName("Should save standard text message and trigger push notification")
    void shouldSaveTextMessageAndPublishEvent() {
        SendMessageRequest request = new SendMessageRequest("Hello!", LocalDateTime.now(), MessageType.TEXT);
        Message mappedMsg = new Message();
        mappedMsg.setMessageType(MessageType.TEXT);
        mappedMsg.setContent("Hello!");

        MessageResponse responseMock = new MessageResponse(
                "msg-1", chatId, senderId, "Hello!", false, ActionType.NEW_MESSAGE, null, LocalDateTime.now()
        );

        when(messageMapper.sendRequestToDomain(request)).thenReturn(mappedMsg);
        when(messageGateway.saveMessage(any(Message.class))).thenReturn(mappedMsg);
        when(messageMapper.domainToResponse(mappedMsg)).thenReturn(responseMock);
        when(chatUseCase.getChatById(chatId)).thenReturn(mockChat);

        messageUseCase.saveMessage(chatId, authUser, request, null);

        verify(messageGateway).saveMessage(mappedMsg);
        verify(chatGateway).updateLastMessage(eq(chatId), eq("Hello!"), any(LocalDateTime.class));
        verify(messageBrokerGateway).convertAndSend(eq("chat.topic"), eq("chat.event." + chatId), eq(responseMock));
        verify(eventUseCase).publishEvent(eq(senderId), eq(receiverId), eq("NEW_MESSAGE"), eq("Igor sent you a new message!"));
        verify(eventUseCase, never()).publishEvent(eq(senderId), eq(senderId), anyString(), anyString());
    }

    @Test
    @DisplayName("Should save call log but SUPPRESS push notifications")
    void shouldSaveCallLogWithoutPushNotification() {
        SendMessageRequest request = new SendMessageRequest("Missed Call", LocalDateTime.now(), MessageType.CALL_LOG);
        Message mappedMsg = new Message();
        mappedMsg.setMessageType(MessageType.CALL_LOG);

        when(messageMapper.sendRequestToDomain(request)).thenReturn(mappedMsg);
        when(messageGateway.saveMessage(any(Message.class))).thenReturn(mappedMsg);

        messageUseCase.saveMessage(chatId, authUser, request, null);

        verify(messageBrokerGateway).convertAndSend(anyString(), anyString(), any());
        verify(eventUseCase, never()).publishEvent(anyLong(), anyLong(), anyString(), anyString());
    }

    @Test
    @DisplayName("Should save file message, upload to storage, and set correct preview")
    void shouldSaveFileMessage() {
        SendMessageRequest request = new SendMessageRequest(null, LocalDateTime.now(), MessageType.FILE);
        Message mappedMsg = new Message();
        mappedMsg.setMessageType(MessageType.FILE);

        Attachment mockAttachment = Attachment.builder()
                .key("aws-key")
                .url("https://s3...")
                .fileName("doc.pdf")
                .contentType("application/pdf")
                .size(1024L)
                .build();

        when(mockFile.isEmpty()).thenReturn(false);
        when(messageMapper.sendRequestToDomain(request)).thenReturn(mappedMsg);
        when(fileStorageGateway.store(mockFile, chatId)).thenReturn(mockAttachment);
        when(messageGateway.saveMessage(any(Message.class))).thenReturn(mappedMsg);

        when(chatUseCase.getChatById(chatId)).thenReturn(mockChat);

        messageUseCase.saveMessage(chatId, authUser, request, mockFile);

        verify(fileStorageGateway).store(mockFile, chatId);
        verify(chatGateway).updateLastMessage(eq(chatId), eq("📎 Attachment: doc.pdf"), any(LocalDateTime.class));
        verify(eventUseCase).publishEvent(eq(senderId), eq(receiverId), eq("NEW_MESSAGE"), eq("Igor sent you a new message!"));
    }

    @Test
    @DisplayName("Should delete message, remove file from storage, and update to previous preview")
    void shouldDeleteMessageAndRevertPreview() {
        String msgId = "msg-123";
        Message existingMsg = new Message();
        existingMsg.setId(msgId);
        existingMsg.setSenderId(senderId);
        existingMsg.setMessageType(MessageType.FILE);
        existingMsg.setAttachment(Attachment.builder().key("file-key").build());

        Message previousMsg = new Message();
        previousMsg.setContent("Older message");
        previousMsg.setCreatedAt(LocalDateTime.now().minusMinutes(5));

        when(messageGateway.findMessageById(msgId)).thenReturn(Optional.of(existingMsg));
        when(messageGateway.findLastMessageByChatId(chatId)).thenReturn(Optional.of(previousMsg));

        messageUseCase.deleteMessage(chatId, msgId, senderId);

        verify(fileStorageGateway).delete("file-key");
        verify(messageGateway).deleteMessage(msgId);
        verify(chatGateway).forceUpdateLastMessagePreview(chatId, "Older message", previousMsg.getCreatedAt());

        verify(messageBrokerGateway).convertAndSend(eq("chat.topic"), eq("chat.event." + chatId), any(MessageDeletedEvent.class));
    }

    @Test
    @DisplayName("Should throw Unauthorized when deleting someone else's message")
    void shouldThrowWhenDeletingOthersMessage() {
        Message existingMsg = new Message();
        existingMsg.setSenderId(99L);

        when(messageGateway.findMessageById("msg-123")).thenReturn(Optional.of(existingMsg));

        assertThrows(UnauthorizedActionException.class, () -> messageUseCase.deleteMessage(chatId, "msg-123", senderId));
        verify(messageGateway, never()).deleteMessage(anyString());
    }

    @Test
    @DisplayName("Should edit message content and set edited flag")
    void shouldEditMessage() {
        EditMessageRequest request = new EditMessageRequest("msg-123", "Edited text");
        Message existingMsg = new Message();
        existingMsg.setSenderId(senderId);
        existingMsg.setContent("Old text");

        when(messageGateway.findMessageById("msg-123")).thenReturn(Optional.of(existingMsg));
        when(messageGateway.updateMessage(any(Message.class))).thenReturn(existingMsg);

        messageUseCase.editMessage(chatId, senderId, request);

        verify(messageGateway).updateMessage(messageCaptor.capture());
        assertEquals("Edited text", messageCaptor.getValue().getContent());
        assertTrue(messageCaptor.getValue().getEdited());
    }

    @Test
    @DisplayName("Should save audio message and notify")
    void shouldSaveAudioMessage() {
        SendAudioRequest request = new SendAudioRequest(LocalDateTime.now(), 15.0);
        Attachment mockAttachment = Attachment.builder()
                .key("audio-key")
                .fileName("audio.webm")
                .contentType("audio/webm")
                .size(2048L)
                .duration(15.0)
                .build();

        when(fileStorageGateway.store(mockFile, chatId)).thenReturn(mockAttachment);
        when(chatUseCase.getChatById(chatId)).thenReturn(mockChat);

        messageUseCase.saveAudioMessage(chatId, authUser, request, mockFile);

        verify(messageGateway).saveMessage(messageCaptor.capture());
        assertEquals(MessageType.AUDIO, messageCaptor.getValue().getMessageType());
        assertEquals(15.0, messageCaptor.getValue().getAttachment().getDuration());

        verify(chatGateway).updateLastMessage(eq(chatId), eq("New audio"), any(LocalDateTime.class));
    }

    @Test
    @DisplayName("Should fetch paginated messages and regenerate S3 presigned URLs")
    void shouldGetMessagesAndRefreshUrls() {
        Message msgWithFile = new Message();
        msgWithFile.setAttachment(Attachment.builder().key("key").url("old-url").build());

        PageImpl<Message> page = new PageImpl<>(List.of(msgWithFile));
        when(messageGateway.getMessagePage(eq(chatId), any(PageRequest.class))).thenReturn(page);
        when(fileStorageGateway.generatePresignedUrl("key")).thenReturn("https://s3.fresh-url.com");
        when(messageMapper.domainToResponse(any())).thenReturn(null);

        MessagePaginatedResponse response = messageUseCase.getMessages(chatId, 0, 10);

        assertEquals(1, response.totalElements());
        assertEquals("https://s3.fresh-url.com", msgWithFile.getAttachment().getUrl());
    }

    @Test
    @DisplayName("Should mark messages as read and broadcast ReadReceiptEvent")
    void shouldMarkAsReadAndBroadcast() {
        when(chatParticipantGateway.updateLastReadAt(eq(chatId), eq(senderId), any(LocalDateTime.class))).thenReturn(1);

        messageUseCase.markMessagesAsRead(chatId, senderId);

        verify(messageBrokerGateway).convertAndSend(eq("chat.topic"), eq("chat.event." + chatId), brokerPayloadCaptor.capture());

        ReadReceiptEvent event = (ReadReceiptEvent) brokerPayloadCaptor.getValue();
        assertEquals(ActionType.READ, event.actionType());
        assertEquals(senderId, event.userId());
    }
}