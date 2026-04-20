package com.microservice.microchatmessagingservice.controller;

import com.microservice.microchatmessagingservice.application.usecases.ChatUseCase;
import com.microservice.microchatmessagingservice.controller.dtos.reponse.ChatResponse;
import com.microservice.microchatmessagingservice.controller.dtos.request.ChatRequest;
import com.microservice.microchatmessagingservice.infrastructure.config.UserAuthenticated;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatUseCase chatUseCase;

    @PostMapping
    public ResponseEntity<ChatResponse> createChat(@RequestBody ChatRequest chatRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(chatUseCase.createChat(chatRequest));
    }

    @GetMapping("/user")
    public ResponseEntity<List<ChatResponse>> getChats(
            @AuthenticationPrincipal UserAuthenticated currentUser
    ) {
        Long userId = currentUser.id();

        return ResponseEntity.ok(chatUseCase.getAllChats(userId));
    }

    @PatchMapping("/{chatId}")
    public ResponseEntity<ChatResponse> updateChat(
            @PathVariable UUID chatId,
            @RequestBody ChatRequest chatRequest
    ) {
        return ResponseEntity.ok(chatUseCase.updateChat(chatId, chatRequest));
    }

    @DeleteMapping("/{chatId}")
    public ResponseEntity<Void> deleteChat(
            @PathVariable UUID chatId,
             @AuthenticationPrincipal UserAuthenticated currentUser
    ) {
        Long userId = currentUser.id();

        chatUseCase.deleteChat(userId, chatId);
        return ResponseEntity.noContent().build();
    }
}