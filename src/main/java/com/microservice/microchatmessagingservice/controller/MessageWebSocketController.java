package com.microservice.microchatmessagingservice.controller;

import com.microservice.microchatmessagingservice.application.usecases.MessageUseCase;
import com.microservice.microchatmessagingservice.controller.dtos.response.ReadReceiptEvent;
import com.microservice.microchatmessagingservice.controller.dtos.request.EditMessageRequest;
import com.microservice.microchatmessagingservice.controller.dtos.response.MessageDeletedEvent;
import com.microservice.microchatmessagingservice.controller.dtos.request.SendMessageRequest;
import com.microservice.microchatmessagingservice.controller.dtos.response.MessageResponse;
import com.microservice.microchatmessagingservice.domain.ActionType;
import com.microservice.microchatmessagingservice.infrastructure.config.UserAuthenticated;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class MessageWebSocketController {

    private final MessageUseCase messageUseCase;
    private final RabbitTemplate rabbitTemplate;

    @MessageMapping("/chat/{chatId}/sendMessage")
    public void sendMessage(
            @DestinationVariable UUID chatId,
            @Payload SendMessageRequest request,
            Principal principal
    ) {

        var auth = (UsernamePasswordAuthenticationToken) principal;
        var currentUser = (UserAuthenticated) auth.getPrincipal();

        MessageResponse savedMessage = messageUseCase.saveMessage(chatId, currentUser.id(), request);

        rabbitTemplate.convertAndSend("chat.topic", "chat.event." + chatId, savedMessage);
    }

    @MessageMapping("/chat/{chatId}/editMessage")
    public void editMessage(
            @DestinationVariable UUID chatId,
            @Payload EditMessageRequest request,
            Principal principal
    ) {

        var auth = (UsernamePasswordAuthenticationToken) principal;
        var currentUser = (UserAuthenticated) auth.getPrincipal();

        MessageResponse editedMessage = messageUseCase.editMessage(chatId, currentUser.id(), request);

        rabbitTemplate.convertAndSend("chat.topic", "chat.event." + chatId, editedMessage);
    }

    @MessageMapping("/chat/{chatId}/deleteMessage")
    public void deleteMessage(
            @DestinationVariable UUID chatId,
            @Payload String messageId,
            Principal principal
    ) {

        var auth = (UsernamePasswordAuthenticationToken) principal;
        var currentUser = (UserAuthenticated) auth.getPrincipal();

        messageUseCase.deleteMessage(chatId, messageId, currentUser.id());

        var event = new MessageDeletedEvent(messageId, chatId, ActionType.DELETE_MESSAGE);

        rabbitTemplate.convertAndSend("chat.topic", "chat.event." + chatId, event);
    }

    @MessageMapping("/chat/{chatId}/read")
    public void handleReadReceipt(
            @DestinationVariable UUID chatId,
            Principal principal
    ) {
        var auth = (UsernamePasswordAuthenticationToken) principal;
        var currentUser = (UserAuthenticated) auth.getPrincipal();

        ReadReceiptEvent event = messageUseCase.markMessagesAsRead(chatId, currentUser.id());

        rabbitTemplate.convertAndSend("chat.topic", "chat.event." + chatId, event);
    }
}
