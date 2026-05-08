package com.microservice.microchatmessagingservice;

import com.microservice.microchatmessagingservice.application.gateways.EventPublisherGateway;
import com.microservice.microchatmessagingservice.application.usecases.EventUseCase;
import com.microservice.microchatmessagingservice.application.usecases.payloads.EventPayload;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class EventUseCaseTest {

    @Mock
    private EventPublisherGateway eventPublisherGateway;

    @InjectMocks
    private EventUseCase eventUseCase;

    @Captor
    private ArgumentCaptor<EventPayload> eventPayloadCaptor;

    @Test
    @DisplayName("Should successfully build payload and route it to the publisher gateway")
    void shouldPublishEventSuccessfully() {
        Long senderId = 100L;
        Long receiverId = 200L;
        String type = "NEW_PRIVATE_MESSAGE";
        String content = "Hey, are we still deploying the cluster today?";

        eventUseCase.publishEvent(senderId, receiverId, type, content);

        verify(eventPublisherGateway, times(1)).publishNotificationEvent(eventPayloadCaptor.capture());

        EventPayload capturedEvent = eventPayloadCaptor.getValue();

        assertNotNull(capturedEvent);
        assertEquals(senderId, capturedEvent.senderId());
        assertEquals(receiverId, capturedEvent.receiverId());
        assertEquals(type, capturedEvent.type());
        assertEquals(content, capturedEvent.content());
    }
}