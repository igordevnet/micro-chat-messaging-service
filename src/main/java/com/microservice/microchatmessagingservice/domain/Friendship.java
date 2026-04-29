package com.microservice.microchatmessagingservice.domain;

import com.microservice.microchatmessagingservice.domain.enums.FriendshipStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Friendship {

    private UUID id;

    private Long requesterId;

    private Long receiverId;

    private FriendshipStatus status;
}
