package com.microservice.microchatmessagingservice.controller;

import com.microservice.microchatmessagingservice.application.usecases.FriendshipUseCase;
import com.microservice.microchatmessagingservice.controller.dtos.request.FriendshipAnswerRequest;
import com.microservice.microchatmessagingservice.controller.dtos.request.FriendshipRequest;
import com.microservice.microchatmessagingservice.infrastructure.config.UserAuthenticated;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/friendship")
@RequiredArgsConstructor
public class FriendshipController {

    private final FriendshipUseCase friendshipUseCase;

    @PostMapping("/request")
    public ResponseEntity<Void> sendRequest(
            @RequestBody FriendshipRequest request,
            @AuthenticationPrincipal UserAuthenticated user
    ) {
        friendshipUseCase.sendFriendshipRequest(request, user.id());
        return ResponseEntity.ok().build();
    }

    @PutMapping("/answer")
    public ResponseEntity<Void> answerRequest(
            @RequestBody FriendshipAnswerRequest request,
            @AuthenticationPrincipal UserAuthenticated user
    ) {
        friendshipUseCase.answerFriendshipRequest(request, user.id());
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{friendshipId}/block")
    public ResponseEntity<Void> blockFriendship(
            @PathVariable UUID friendshipId,
            @AuthenticationPrincipal UserAuthenticated user
    ) {
        friendshipUseCase.blockFriendship(friendshipId, user.id());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/friends")
    public ResponseEntity<List<Long>> getAcceptedFriends(@AuthenticationPrincipal UserAuthenticated user) {
        List<Long> friends = friendshipUseCase.getAcceptedFriendIds(user.id());
        return ResponseEntity.ok(friends);
    }
}
