package com.microservice.microchatmessagingservice.infrastructure.gateways;

import com.microservice.microchatmessagingservice.application.exceptions.FriendshipNotFound;
import com.microservice.microchatmessagingservice.application.gateways.FriendshipGateway;
import com.microservice.microchatmessagingservice.domain.Friendship;
import com.microservice.microchatmessagingservice.infrastructure.persistence.mappers.FriendshipMapper;
import com.microservice.microchatmessagingservice.infrastructure.persistence.postgre.FriendshipRepository;
import com.microservice.microchatmessagingservice.infrastructure.persistence.postgre.entities.FriendshipEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class FriendshipRepositoryGateway implements FriendshipGateway {

    private final FriendshipRepository friendshipRepository;
    private final FriendshipMapper friendshipMapper;

    @Override
    public Friendship saveFriendship(Friendship friendship) {
        FriendshipEntity friendshipEntity = friendshipMapper.domainToEntity(friendship);

        var savedFriendship = friendshipRepository.save(friendshipEntity);

        return friendshipMapper.entityToDomain(savedFriendship);
    }

    @Override
    public Friendship getFriendshipById(UUID uuid) {
        return friendshipRepository.findById(uuid)
                .map(friendshipMapper::entityToDomain)
                .orElseThrow(() -> (new FriendshipNotFound("Friendship does not exist")));
    }

    @Override
    public List<Long> getAcceptedFriendIds(Long userId) {
        return friendshipRepository.findAcceptedFriendIdsByUserId(userId)
                .stream()
                .map(f -> f.getRequesterId().equals(userId) ? f.getReceiverId() : f.getRequesterId())
                .toList();
    }

    @Override
    public Boolean existsByUsers(Long requesterId, Long receiverId) {
        return friendshipRepository.existsBetweenUsers(requesterId, receiverId);
    }
}
