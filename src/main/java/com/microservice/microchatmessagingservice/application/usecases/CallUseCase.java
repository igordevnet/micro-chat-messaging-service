package com.microservice.microchatmessagingservice.application.usecases;

import com.microservice.microchatmessagingservice.application.gateways.MessageBrokerGateway;
import com.microservice.microchatmessagingservice.controller.dtos.request.SendMessageRequest;
import com.microservice.microchatmessagingservice.controller.dtos.request.SignalingRequest;
import com.microservice.microchatmessagingservice.controller.dtos.response.SignalingPayload;
import com.microservice.microchatmessagingservice.domain.enums.MessageType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CallUseCase {

    private final MessageBrokerGateway messageBrokerGateway;
    private final MessageUseCase messageUseCase;

    public void handleCall(SignalingRequest request, Long senderId ) {

        var signalingPayload = SignalingPayload.builder()
                .type(request.type())
                .chatId(request.chatId())
                .senderId(senderId)
                .targetId(request.targetId())
                .data(request.data())
                .build();

        messageBrokerGateway.convertAndSend(
                "chat.topic",
                "chat.event." + request.targetId(),
                signalingPayload
        );

        handleCallType(request, senderId);
    }

    private void handleCallType(SignalingRequest request, Long senderId) {
        switch (request.type()) {
            case REJECTED:
                saveCallLogToDatabase(request.chatId(), senderId, "Voice Call Rejected");
                break;

            case HANG_UP:
                saveCallLogToDatabase(request.chatId(), senderId, "Voice Call Ended");
                break;

            case MISSED:
                saveCallLogToDatabase(request.chatId(), senderId, "Missed Voice Call");
                break;
        }
    }

    private void saveCallLogToDatabase(UUID chatId, Long senderId, String content) {
        SendMessageRequest logMessage = SendMessageRequest.builder()
                .messageType(MessageType.CALL_LOG)
                .content(content)
                .createdAt(LocalDateTime.now())
                .build();

        messageUseCase.saveMessage(chatId, senderId, logMessage, null);
    }
}
