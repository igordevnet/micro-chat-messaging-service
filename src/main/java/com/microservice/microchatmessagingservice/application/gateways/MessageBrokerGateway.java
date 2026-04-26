package com.microservice.microchatmessagingservice.application.gateways;

public interface MessageBrokerGateway {
    void convertAndSend(String topic, String destination, Object message);
}
