package com.microservice.microchatmessagingservice.application.usecases;

import com.microservice.microchatmessagingservice.application.gateways.MessageBrokerGateway;
import com.microservice.microchatmessagingservice.controller.dtos.request.SendMessageRequest;
import com.microservice.microchatmessagingservice.controller.dtos.request.SignalingRequest;
import com.microservice.microchatmessagingservice.controller.dtos.response.SignalingPayload;
import com.microservice.microchatmessagingservice.domain.enums.MessageType;
import com.microservice.microchatmessagingservice.infrastructure.config.UserAuthenticated;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CallUseCase {

    private final MessageBrokerGateway messageBrokerGateway;
    private final MessageUseCase messageUseCase;

    public void handleCall(SignalingRequest request, UserAuthenticated user) {

        var signalingPayload = SignalingPayload.builder()
                .type(request.type())
                .chatId(request.chatId())
                .senderId(user.id())
                .targetId(request.targetId())
                .data(request.data())
                .isVideo(request.isVideo())
                .build();

        sendToBroker(signalingPayload);

        handleCallType(request, user);
    }

    private void handleCallType(SignalingRequest request, UserAuthenticated user) {
        switch (request.type()) {
            case REJECTED:
                saveCallLogToDatabase(request.chatId(), user, "Voice Call Rejected");
                break;

            case HANG_UP:
                saveCallLogToDatabase(request.chatId(), user, "Voice Call Ended");
                break;

            case MISSED:
                saveCallLogToDatabase(request.chatId(), user, "Missed Voice Call");
                break;
        }
    }

    private void saveCallLogToDatabase(UUID chatId, UserAuthenticated user, String content) {
        SendMessageRequest logMessage = SendMessageRequest.builder()
                .messageType(MessageType.CALL_LOG)
                .content(content)
                .createdAt(LocalDateTime.now())
                .build();

        messageUseCase.saveMessage(chatId, user, logMessage, null);
    }

    private void sendToBroker(SignalingPayload signalingPayload) {
        messageBrokerGateway.convertAndSend(
                "chat.topic",
                "chat.event." + signalingPayload.targetId(),
                signalingPayload
        );
    }
}
