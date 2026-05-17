package com.microservice.microchatmessagingservice.application.usecases;

import com.microservice.microchatmessagingservice.application.gateways.EventPublisherGateway;
import com.microservice.microchatmessagingservice.application.usecases.payloads.EventPayload;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EventUseCase {

    private final EventPublisherGateway eventPublisherGateway;

    public void publishEvent(Long senderId, Long receiverId, UUID chatId, String type, String content){
        var event = EventPayload.builder()
                .senderId(senderId)
                .receiverId(receiverId)
                .chatId(chatId)
                .type(type)
                .content(content)
                .build();

        eventPublisherGateway.publishNotificationEvent(event);
    }
}
