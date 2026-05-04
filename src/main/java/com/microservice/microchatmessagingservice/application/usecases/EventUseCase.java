package com.microservice.microchatmessagingservice.application.usecases;

import com.microservice.microchatmessagingservice.application.gateways.EventPublisherGateway;
import com.microservice.microchatmessagingservice.application.usecases.payloads.EventPayload;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EventUseCase {

    private final EventPublisherGateway eventPublisherGateway;

    public void publishEvent(Long senderId, Long receiverId, String type, String content){
        var event = EventPayload.builder()
                .senderId(senderId)
                .receiverId(receiverId)
                .type(type)
                .content(content)
                .build();

        eventPublisherGateway.publishNotificationEvent(event);
    }
}
