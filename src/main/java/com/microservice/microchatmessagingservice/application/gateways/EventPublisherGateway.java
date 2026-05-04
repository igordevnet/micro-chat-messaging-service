package com.microservice.microchatmessagingservice.application.gateways;

import com.microservice.microchatmessagingservice.application.usecases.payloads.EventPayload;

public interface EventPublisherGateway {
    void publishNotificationEvent(EventPayload event);
}
