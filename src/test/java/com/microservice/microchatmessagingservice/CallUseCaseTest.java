package com.microservice.microchatmessagingservice;

import com.microservice.microchatmessagingservice.application.gateways.MessageBrokerGateway;
import com.microservice.microchatmessagingservice.application.usecases.CallUseCase;
import com.microservice.microchatmessagingservice.application.usecases.MessageUseCase;
import com.microservice.microchatmessagingservice.controller.dtos.request.SendMessageRequest;
import com.microservice.microchatmessagingservice.controller.dtos.request.SignalingRequest;
import com.microservice.microchatmessagingservice.controller.dtos.response.SignalingPayload;
import com.microservice.microchatmessagingservice.domain.enums.MessageType;
import com.microservice.microchatmessagingservice.domain.enums.SignalingType;
import com.microservice.microchatmessagingservice.infrastructure.config.UserAuthenticated;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CallUseCaseTest {

    @InjectMocks
    private CallUseCase callUseCase;

    @Mock
    private MessageBrokerGateway messageBrokerGateway;

    @Mock
    private MessageUseCase messageUseCase;

    @Captor
    private ArgumentCaptor<SignalingPayload> payloadCaptor;

    @Captor
    private ArgumentCaptor<SendMessageRequest> messageRequestCaptor;

    private UserAuthenticated mockUser;
    private UUID chatId;
    private Long targetId;

    @BeforeEach
    void setUp() {
        chatId = UUID.randomUUID();
        targetId = 2L;

        mockUser = new UserAuthenticated(
                1L,
                "test@example.com",
                "ADMIN"
        );
    }

    @Test
    @DisplayName("Should route standard WebRTC signals (OFFER/ANSWER) to broker without saving to DB")
    void shouldRouteSignalToBrokerWithoutSavingLog() {
        SignalingRequest request = new SignalingRequest(
                SignalingType.OFFER,
                targetId,
                chatId,
                "sdp-data-here"
        );

        callUseCase.handleCall(request, mockUser);

        verify(messageBrokerGateway, times(1)).convertAndSend(
                eq("chat.topic"),
                eq("chat.event." + targetId),
                payloadCaptor.capture()
        );

        SignalingPayload capturedPayload = payloadCaptor.getValue();
        assertEquals(SignalingType.OFFER, capturedPayload.type());
        assertEquals(chatId, capturedPayload.chatId());
        assertEquals(mockUser.id(), capturedPayload.senderId());
        assertEquals(targetId, capturedPayload.targetId());
        assertEquals("sdp-data-here", capturedPayload.data());

        verify(messageUseCase, never()).saveMessage(any(), any(), any(), any());
    }

    @Test
    @DisplayName("Should route signal and save 'Voice Call Rejected' log to DB")
    void shouldSaveLogWhenCallRejected() {
        SignalingRequest request = new SignalingRequest(
                SignalingType.REJECTED,
                targetId,
                chatId,
                null
        );

        callUseCase.handleCall(request, mockUser);

        verifyBrokerRouting(SignalingType.REJECTED);
        verifyDatabaseLog("Voice Call Rejected");
    }

    @Test
    @DisplayName("Should route signal and save 'Voice Call Ended' log to DB")
    void shouldSaveLogWhenCallHungUp() {
        SignalingRequest request = new SignalingRequest(
                SignalingType.HANG_UP,
                targetId,
                chatId,
                null
        );

        callUseCase.handleCall(request, mockUser);

        verifyBrokerRouting(SignalingType.HANG_UP);
        verifyDatabaseLog("Voice Call Ended");
    }

    @Test
    @DisplayName("Should route signal and save 'Missed Voice Call' log to DB")
    void shouldSaveLogWhenCallMissed() {
        SignalingRequest request = new SignalingRequest(
                SignalingType.MISSED,
                targetId,
                chatId,
                null
        );

        callUseCase.handleCall(request, mockUser);

        verifyBrokerRouting(SignalingType.MISSED);
        verifyDatabaseLog("Missed Voice Call");
    }

    private void verifyBrokerRouting(SignalingType expectedType) {
        verify(messageBrokerGateway, times(1)).convertAndSend(
                eq("chat.topic"),
                eq("chat.event." + targetId),
                any(SignalingPayload.class)
        );
    }

    private void verifyDatabaseLog(String expectedContent) {
        verify(messageUseCase, times(1)).saveMessage(
                eq(chatId),
                eq(mockUser),
                messageRequestCaptor.capture(),
                isNull()
        );

        SendMessageRequest capturedMessage = messageRequestCaptor.getValue();
        assertEquals(MessageType.CALL_LOG, capturedMessage.messageType());
        assertEquals(expectedContent, capturedMessage.content());
        assertNotNull(capturedMessage.createdAt());
    }
}