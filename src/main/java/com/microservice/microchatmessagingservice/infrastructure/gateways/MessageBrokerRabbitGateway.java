package com.microservice.microchatmessagingservice.infrastructure.gateways;

import com.microservice.microchatmessagingservice.application.gateways.MessageBrokerGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MessageBrokerRabbitGateway implements MessageBrokerGateway {

    private final RabbitTemplate rabbitTemplate;

    @Override
    public void convertAndSend(String topic, String destination, Object message) {
        rabbitTemplate.convertAndSend(topic, destination, message);
    }
}
