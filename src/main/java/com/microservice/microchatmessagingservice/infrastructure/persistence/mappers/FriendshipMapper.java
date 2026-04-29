package com.microservice.microchatmessagingservice.infrastructure.persistence.mappers;

import com.microservice.microchatmessagingservice.controller.dtos.request.FriendshipAnswerRequest;
import com.microservice.microchatmessagingservice.controller.dtos.request.FriendshipRequest;
import com.microservice.microchatmessagingservice.controller.dtos.response.FriendshipResponse;
import com.microservice.microchatmessagingservice.domain.Friendship;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface FriendshipMapper {

    Friendship requestToDomain(FriendshipRequest friendshipRequest);

    Friendship answerToDomain(FriendshipAnswerRequest friendshipAnswerRequest);

    FriendshipResponse domainToResponse(Friendship friendship);
}
