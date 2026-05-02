package com.microservice.microchatmessagingservice.application.gateways;

public interface MessageBrokerGateway {
    void convertAndSend(String exchange, String routingKey, Object message);
}
