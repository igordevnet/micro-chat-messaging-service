package com.microservice.microchatmessagingservice.application.usecases;

import com.microservice.microchatmessagingservice.application.exceptions.MessageNotFoundException;
import com.microservice.microchatmessagingservice.application.exceptions.UnauthorizedActionException;
import com.microservice.microchatmessagingservice.application.gateways.MessageGateway;
import com.microservice.microchatmessagingservice.controller.dtos.reponse.MessageResponse;
import com.microservice.microchatmessagingservice.controller.dtos.request.EditMessageRequest;
import com.microservice.microchatmessagingservice.controller.dtos.request.SendMessageRequest;
import com.microservice.microchatmessagingservice.domain.Message;
import com.microservice.microchatmessagingservice.infrastructure.persistence.mappers.MessageMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageUseCase {

    private final MessageGateway messageGateway;
    private final MessageMapper messageMapper;

    public MessageResponse saveMessage(
            UUID chatId,
            Long userId,
            SendMessageRequest messageRequest
    ) {
        Message message = messageMapper.sendRequestToDomain(messageRequest);
        message.setChatId(chatId);
        message.setSenderId(userId);

        var savedMessage = messageGateway.saveMessage(message);

        return messageMapper.domainToResponse(savedMessage);
    }

    public void deleteMessage(String messageId, Long userId) {
        Message message = messageGateway.findMessageById(messageId)
                .orElseThrow(() -> new MessageNotFoundException("Message not found"));

        throwIfUserCannotDeleteTheMessage(userId, message.getSenderId());

        messageGateway.deleteMessage(messageId);
    }

    public MessageResponse editMessage(
            UUID chatId,
            Long userId,
            EditMessageRequest messageRequest
    ) {
        Message message = messageMapper.editRequestToDomain(messageRequest);
        message.setChatId(chatId);
        message.setSenderId(userId);
        message.setEdited(true);

        var editedMessage = messageGateway.updateMessage(message);

        return messageMapper.domainToResponse(editedMessage);
    }

    public Page<MessageResponse> getMessages(UUID chatId, int page, int size) {

        Pageable pageable = PageRequest.of(page, size);

        Page<Message> messages = messageGateway.getMessagePage(chatId, pageable);

        return messages.map(messageMapper::domainToResponse);
    }

    private void throwIfUserCannotDeleteTheMessage(Long userId, Long senderId) {
        if (!senderId.equals(userId)) {
            throw new UnauthorizedActionException("You can't delete this message");
        }
    }
}
