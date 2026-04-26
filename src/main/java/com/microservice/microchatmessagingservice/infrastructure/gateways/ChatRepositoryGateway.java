package com.microservice.microchatmessagingservice.infrastructure.gateways;

import com.microservice.microchatmessagingservice.application.gateways.ChatGateway;
import com.microservice.microchatmessagingservice.domain.Chat;
import com.microservice.microchatmessagingservice.infrastructure.persistence.mappers.ChatMapper;
import com.microservice.microchatmessagingservice.infrastructure.persistence.postgre.ChatParticipantRepository;
import com.microservice.microchatmessagingservice.infrastructure.persistence.postgre.ChatRepository;
import com.microservice.microchatmessagingservice.infrastructure.persistence.postgre.entities.ChatEntity;
import com.microservice.microchatmessagingservice.infrastructure.persistence.postgre.entities.ChatParticipantEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatRepositoryGateway implements ChatGateway {

    private final ChatRepository chatRepository;
    private final ChatParticipantRepository participantRepository;
    private final ChatMapper chatMapper;

    @Override
    @Transactional
    public Chat saveChat(Chat chat) {

        ChatEntity chatEntity = chatMapper.domainToEntity(chat);
        List<ChatParticipantEntity> participantsToSave = chatEntity.getParticipants();
        chatEntity.setParticipants(null);

        ChatEntity savedChat = chatRepository.save(chatEntity);

        if (participantsToSave != null) {
            List<ChatParticipantEntity> savedParticipants = participantsToSave.stream()
                    .map(p -> {
                        p.setChat(savedChat);
                        return participantRepository.save(p);
                    }).toList();

            savedChat.setParticipants(savedParticipants);
        }

        return chatMapper.entityToDomain(savedChat);
    }


    @Override
    public void deleteChat(UUID chatId) {
        chatRepository.deleteById(chatId);
    }

    @Override
    public List<Chat> getChatList(Long userId) {
        List<ChatEntity> chatEntities = chatRepository.findAllByParticipants(userId);

        return chatMapper.entityToDomainList(chatEntities);
    }

    @Override
    public Optional<Chat> getChat(UUID chatId) {
        return chatRepository.findById(chatId)
                .map(chatMapper::entityToDomain);
    }

    @Override
    public void updateLastMessage(UUID chatId, String preview, LocalDateTime time) {
        chatRepository.updateLastMessageIfNewer(chatId, preview, time);
    }

    @Override
    public void forceUpdateLastMessagePreview(UUID chatId, String preview, LocalDateTime time) {
        chatRepository.forceUpdateLastMessage(chatId, preview, time);
    }
}
