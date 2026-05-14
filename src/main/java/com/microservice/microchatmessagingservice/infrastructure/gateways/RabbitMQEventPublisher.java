package com.microservice.microchatmessagingservice.infrastructure.gateways;

import com.microservice.microchatmessagingservice.application.gateways.EventPublisherGateway;
import com.microservice.microchatmessagingservice.application.usecases.payloads.EventPayload;
import com.microservice.microchatmessagingservice.controller.dtos.NotificationEventDto;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class RabbitMQEventPublisher implements EventPublisherGateway {

    private final RabbitTemplate rabbitTemplate;

    @Override
    public void publishNotificationEvent(EventPayload eventPayload) {

        var eventDto = new NotificationEventDto(
                eventPayload.senderId(),
                eventPayload.receiverId(),
                eventPayload.type(),
                eventPayload.content(),
                eventPayload.chatId(),
                LocalDateTime.now()
        );

        rabbitTemplate.convertAndSend(
                "chat.topic",
                "system.notification.chat",
                eventDto
        );
    }
}