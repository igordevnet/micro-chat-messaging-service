package com.microservice.microchatmessagingservice.application.usecases;

import com.microservice.microchatmessagingservice.application.exceptions.MessageNotFoundException;
import com.microservice.microchatmessagingservice.application.exceptions.UnauthorizedActionException;
import com.microservice.microchatmessagingservice.application.gateways.ChatGateway;
import com.microservice.microchatmessagingservice.application.gateways.ChatParticipantGateway;
import com.microservice.microchatmessagingservice.application.gateways.MessageGateway;
import com.microservice.microchatmessagingservice.controller.dtos.response.ReadReceiptEvent;
import com.microservice.microchatmessagingservice.controller.dtos.response.MessagePaginatedResponse;
import com.microservice.microchatmessagingservice.controller.dtos.response.MessageResponse;
import com.microservice.microchatmessagingservice.controller.dtos.request.EditMessageRequest;
import com.microservice.microchatmessagingservice.controller.dtos.request.SendMessageRequest;
import com.microservice.microchatmessagingservice.domain.Message;
import com.microservice.microchatmessagingservice.domain.ActionType;
import com.microservice.microchatmessagingservice.infrastructure.persistence.mappers.MessageMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageUseCase {

    private final MessageGateway messageGateway;
    private final ChatParticipantGateway chatParticipantGateway;
    private final ChatGateway chatGateway;
    private final MessageMapper messageMapper;

    @CacheEvict(value = "messages", key = "#chatId.toString() + '*'", allEntries = false)
    public MessageResponse saveMessage(
            UUID chatId,
            Long userId,
            SendMessageRequest messageRequest
    ) {
        Message message = messageMapper.sendRequestToDomain(messageRequest);
        message.setChatId(chatId);
        message.setSenderId(userId);

        var savedMessage = messageGateway.saveMessage(message);
        savedMessage.setActionType(ActionType.NEW_MESSAGE);

        chatGateway.updateLastMessage(chatId, savedMessage.getContent(),  savedMessage.getCreatedAt());

        return messageMapper.domainToResponse(savedMessage);
    }

    @CacheEvict(value = "messages", key = "#chatId + '*'", allEntries = false)
    public void deleteMessage(UUID chatId, String messageId, Long userId) {
        Message message = messageGateway.findMessageById(messageId)
                .orElseThrow(() -> new MessageNotFoundException("Message not found"));

        throwIfUserCannotDeleteTheMessage(userId, message.getSenderId());

        messageGateway.deleteMessage(messageId);

        Optional<Message> newLastMessage = messageGateway.findLastMessageByChatId(chatId);

        if (newLastMessage.isPresent()) {
            Message previousMsg = newLastMessage.get();
            chatGateway.forceUpdateLastMessagePreview(chatId, previousMsg.getContent(), previousMsg.getCreatedAt());
        } else {
            chatGateway.forceUpdateLastMessagePreview(chatId, null, null);
        }
    }

    @CacheEvict(value = "messages", key = "#chatId.toString() + '*'", allEntries = false)
    public MessageResponse editMessage(
            UUID chatId,
            Long userId,
            EditMessageRequest messageRequest
    ) {
        Message existingMessage = messageGateway.findMessageById(messageRequest.id())
                .orElseThrow(() -> new MessageNotFoundException("Message not found"));

        throwIfUserCannotDeleteTheMessage(userId, existingMessage.getSenderId());

        existingMessage.setContent(messageRequest.content());
        existingMessage.setEdited(true);

        var editedMessage = messageGateway.updateMessage(existingMessage);

        editedMessage.setActionType(ActionType.EDIT_MESSAGE);

        return messageMapper.domainToResponse(editedMessage);
    }

    @Cacheable(value = "messages", key = "#chatId.toString() + '_' + #page + '_' + #size")
    public MessagePaginatedResponse getMessages(UUID chatId, int page, int size) {

        Pageable pageable = PageRequest.of(page, size);

        Page<Message> messagePage = messageGateway.getMessagePage(chatId, pageable);

        List<MessageResponse> responses = messagePage.getContent().stream()
                .map(messageMapper::domainToResponse)
                .collect(Collectors.toList());

        return new MessagePaginatedResponse(
                responses,
                messagePage.getNumber(),
                messagePage.getTotalPages(),
                messagePage.getTotalElements()
        );
    }


    public ReadReceiptEvent markMessagesAsRead(UUID chatId, Long userId) {
        LocalDateTime now = LocalDateTime.now();

        int rowsAffected = chatParticipantGateway.updateLastReadAt(chatId, userId, now);

        if (rowsAffected > 0) {
            return ReadReceiptEvent.builder()
                    .userId(userId)
                    .chatId(chatId)
                    .time(now)
                    .actionType(ActionType.READ)
                    .build();
        }

        return null;
    }

    private void throwIfUserCannotDeleteTheMessage(Long userId, Long senderId) {
        if (!senderId.equals(userId)) {
            throw new UnauthorizedActionException("You can't delete this message");
        }
    }
}
