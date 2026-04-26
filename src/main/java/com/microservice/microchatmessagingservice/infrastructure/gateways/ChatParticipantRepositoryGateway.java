package com.microservice.microchatmessagingservice.infrastructure.gateways;

import com.microservice.microchatmessagingservice.application.gateways.ChatParticipantGateway;
import com.microservice.microchatmessagingservice.infrastructure.persistence.postgre.ChatParticipantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatParticipantRepositoryGateway implements ChatParticipantGateway {

    private final ChatParticipantRepository repository;

    @Override
    public int updateLastReadAt(UUID chatId, Long userId, LocalDateTime now) {
        return repository.updateLastReadAt(chatId, userId, now);
    }
}
