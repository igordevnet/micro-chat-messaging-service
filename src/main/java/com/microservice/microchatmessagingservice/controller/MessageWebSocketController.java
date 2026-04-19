package com.microservice.microchatmessagingservice.controller;

import com.microservice.microchatmessagingservice.application.usecases.MessageUseCase;
import com.microservice.microchatmessagingservice.controller.dtos.request.EditMessageRequest;
import com.microservice.microchatmessagingservice.controller.dtos.request.MessageDeletedEvent;
import com.microservice.microchatmessagingservice.controller.dtos.request.SendMessageRequest;
import com.microservice.microchatmessagingservice.controller.dtos.reponse.MessageResponse;
import com.microservice.microchatmessagingservice.infrastructure.config.UserAuthenticated;
import lombok.RequiredArgsConstructor;
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

    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/chat/{chatId}/sendMessage")
    public void sendMessage(
            @DestinationVariable UUID chatId,
            @Payload SendMessageRequest request,
            Principal principal
    ) {

        var auth = (UsernamePasswordAuthenticationToken) principal;
        var currentUser = (UserAuthenticated) auth.getPrincipal();

        MessageResponse savedMessage = messageUseCase.saveMessage(chatId, currentUser.id(), request);

        messagingTemplate.convertAndSend("/topic/chat/" + chatId, savedMessage);
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

        messagingTemplate.convertAndSend("/topic/chat/" + chatId, editedMessage);
    }

    @MessageMapping("/chat/{chatId}/deleteMessage")
    public void deleteMessage(
            @DestinationVariable UUID chatId,
            @Payload String messageId,
            Principal principal
    ) {

        var auth = (UsernamePasswordAuthenticationToken) principal;
        var currentUser = (UserAuthenticated) auth.getPrincipal();

        messageUseCase.deleteMessage(messageId, currentUser.id());

        var event = new MessageDeletedEvent(messageId, "DELETED");

        messagingTemplate.convertAndSend("/topic/chat/" + chatId, event);
    }
}
