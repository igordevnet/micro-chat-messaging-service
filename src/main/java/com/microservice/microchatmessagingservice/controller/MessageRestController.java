package com.microservice.microchatmessagingservice.controller;

import com.microservice.microchatmessagingservice.application.usecases.MessageUseCase;
import com.microservice.microchatmessagingservice.controller.dtos.request.SendMessageRequest;
import com.microservice.microchatmessagingservice.controller.dtos.response.MessagePaginatedResponse;
import com.microservice.microchatmessagingservice.infrastructure.config.UserAuthenticated;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/message")
@RequiredArgsConstructor
public class MessageRestController {

    private final MessageUseCase messageUseCase;

    @GetMapping("/{chatId}")
    public ResponseEntity<MessagePaginatedResponse> getMessages(
            @PathVariable UUID chatId,
            @RequestParam int page,
            @RequestParam int size
    ) {
        return ResponseEntity.ok(messageUseCase.getMessages(chatId, page,  size));
    }

    @PostMapping(value = "/{chatId}/messages", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> sendMessage(
            @PathVariable UUID chatId,
            @RequestPart("data") SendMessageRequest request,
            @RequestPart(value = "file", required = false) MultipartFile file,
            @AuthenticationPrincipal UserAuthenticated user
    ) {
        messageUseCase.saveMessage(chatId, user.id(), request, file);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
