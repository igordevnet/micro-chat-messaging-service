package com.microservice.microchatmessagingservice.infrastructure.config.rabbitmq;

import com.microservice.microchatmessagingservice.controller.dtos.response.MessageDeletedEvent;
import com.microservice.microchatmessagingservice.controller.dtos.response.MessageResponse;
import com.microservice.microchatmessagingservice.controller.dtos.response.ReadReceiptEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@RabbitListener(queues = "#{autoDeleteQueue.name}")
public class ChatListener {

    private final SimpMessagingTemplate messagingTemplate;

    @RabbitHandler
    public void handleMessage(MessageResponse message) {
        String destination = "/topic/chat." + message.chatId();
        messagingTemplate.convertAndSend(destination, message);
        System.out.println("Message sent to: " + destination);
    }

    @RabbitHandler
    public void handleReadReceipt(ReadReceiptEvent event) {
        String destination = "/topic/chat." + event.chatId();
        messagingTemplate.convertAndSend(destination, event);
        System.out.println("Read Event sent to: " + destination);
    }

    @RabbitHandler
    public void handleDelete(MessageDeletedEvent event) {
        String destination = "/topic/chat." + event.chatId();
        messagingTemplate.convertAndSend(destination, event);
        System.out.println("Delete event sent to: " + destination);
    }

    @RabbitHandler(isDefault = true)
    public void onDefault(Object object) {
        System.out.println("Unknown event: " + object);
    }
}
