package com.microservice.microchatmessagingservice.application.usecases;

import com.microservice.microchatmessagingservice.application.exceptions.MessageNotFoundException;
import com.microservice.microchatmessagingservice.application.exceptions.UnauthorizedActionException;
import com.microservice.microchatmessagingservice.application.gateways.*;
import com.microservice.microchatmessagingservice.controller.dtos.response.MessageDeletedEvent;
import com.microservice.microchatmessagingservice.controller.dtos.response.ReadReceiptEvent;
import com.microservice.microchatmessagingservice.controller.dtos.response.MessagePaginatedResponse;
import com.microservice.microchatmessagingservice.controller.dtos.response.MessageResponse;
import com.microservice.microchatmessagingservice.controller.dtos.request.EditMessageRequest;
import com.microservice.microchatmessagingservice.controller.dtos.request.SendMessageRequest;
import com.microservice.microchatmessagingservice.domain.Attachment;
import com.microservice.microchatmessagingservice.domain.Message;
import com.microservice.microchatmessagingservice.domain.enums.ActionType;
import com.microservice.microchatmessagingservice.domain.enums.MessageType;
import com.microservice.microchatmessagingservice.infrastructure.persistence.mappers.MessageMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

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
    private final MessageBrokerGateway messageBrokerGateway;
    private final FileStorageGateway fileStorageGateway;

    @CacheEvict(value = "messages", key = "#chatId.toString() + '*'", allEntries = true)
    @Transactional
    public void saveMessage(
            UUID chatId,
            Long userId,
            SendMessageRequest messageRequest,
            MultipartFile file
    ) {
        LocalDateTime now = LocalDateTime.now();

        Message message = messageMapper.sendRequestToDomain(messageRequest);
        message.setChatId(chatId);
        message.setSenderId(userId);
        message.setCreatedAt(now);

        var handledMessage = handleMessageType(message, file);

        var savedMessage = messageGateway.saveMessage(handledMessage);
        savedMessage.setActionType(ActionType.NEW_MESSAGE);

        String preview = determinePreview(savedMessage);
        chatGateway.updateLastMessage(chatId, preview, now);

        var messageResponse = messageMapper.domainToResponse(savedMessage);

        messageBrokerGateway.convertAndSend("chat.topic", "chat.event." + chatId, messageResponse);
    }

    @CacheEvict(value = "messages", key = "#chatId + '*'", allEntries = true)
    @Transactional
    public void deleteMessage(UUID chatId, String messageId, Long userId) {
        Message message = messageGateway.findMessageById(messageId)
                .orElseThrow(() -> new MessageNotFoundException("Message not found"));

        throwIfUserCannotDeleteTheMessage(userId, message.getSenderId());

        if (message.getMessageType() == MessageType.FILE && message.getAttachment() != null) {
            fileStorageGateway.delete(message.getAttachment().getKey());
        }

        messageGateway.deleteMessage(messageId);

        Optional<Message> newLastMessage = messageGateway.findLastMessageByChatId(chatId);

        if (newLastMessage.isPresent()) {
            Message previousMsg = newLastMessage.get();
            chatGateway.forceUpdateLastMessagePreview(chatId, previousMsg.getContent(), previousMsg.getCreatedAt());
        } else {
            chatGateway.forceUpdateLastMessagePreview(chatId, null, null);
        }

        var event = new MessageDeletedEvent(messageId, chatId, ActionType.DELETE_MESSAGE);

        messageBrokerGateway.convertAndSend("chat.topic", "chat.event." + chatId, event);
    }

    @CacheEvict(value = "messages", key = "#chatId.toString() + '*'", allEntries = true)
    @Transactional
    public void editMessage(
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

        var messageResponse = messageMapper.domainToResponse(editedMessage);

        messageBrokerGateway.convertAndSend("chat.topic", "chat.event." + chatId, messageResponse);
    }

    public MessagePaginatedResponse getMessages(UUID chatId, int page, int size) {
        Page<Message> messagePage = getCachedMessages(chatId, page, size);

        List<MessageResponse> responses = messagePage.getContent().stream()
                .map(message -> {
                    if (message.getAttachment() != null) {
                        String freshUrl = fileStorageGateway.generatePresignedUrl(message.getAttachment().getKey());

                        message.getAttachment().setUrl(freshUrl);
                    }
                    return messageMapper.domainToResponse(message);
                })
                .collect(Collectors.toList());

        return new MessagePaginatedResponse(
                responses,
                messagePage.getNumber(),
                messagePage.getTotalPages(),
                messagePage.getTotalElements()
        );
    }

    @Transactional
    public void markMessagesAsRead(UUID chatId, Long userId) {
        LocalDateTime now = LocalDateTime.now();

        int rowsAffected = chatParticipantGateway.updateLastReadAt(chatId, userId, now);

        if (rowsAffected > 0) {
            var event = ReadReceiptEvent.builder()
                    .userId(userId)
                    .chatId(chatId)
                    .time(now)
                    .actionType(ActionType.READ)
                    .build();

            messageBrokerGateway.convertAndSend("chat.topic", "chat.event." + chatId, event);
        }
    }

    @Cacheable(value = "messages", key = "#chatId.toString() + '_' + #page + '_' + #size")
    private Page<Message> getCachedMessages(UUID chatId, int page, int size) {

        Pageable pageable = PageRequest.of(page, size);

        return messageGateway.getMessagePage(chatId, pageable);
    }

    private void throwIfUserCannotDeleteTheMessage(Long userId, Long senderId) {
        if (!senderId.equals(userId)) {
            throw new UnauthorizedActionException("You can't delete this message");
        }
    }

    private Message handleMessageType(Message message, MultipartFile file) {

        if (file != null && !file.isEmpty()) {
            Attachment attachment = fileStorageGateway.store(file, message.getChatId());

            message.setAttachment(attachment);

            message.setMessageType(MessageType.FILE);
        } else {
            message.setMessageType(MessageType.TEXT);
        }

        return message;
    }

    private String determinePreview(Message message) {
        if (message.getMessageType() == MessageType.FILE && message.getAttachment() != null) {
            return "📎 Attachment: " + message.getAttachment().getFileName();
        }
        return message.getContent();
    }
}
