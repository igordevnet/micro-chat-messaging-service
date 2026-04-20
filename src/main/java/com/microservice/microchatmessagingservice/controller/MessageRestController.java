package com.microservice.microchatmessagingservice.controller;

import com.microservice.microchatmessagingservice.application.usecases.MessageUseCase;
import com.microservice.microchatmessagingservice.controller.dtos.reponse.MessageResponse;
import com.microservice.microchatmessagingservice.infrastructure.config.UserAuthenticated;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/message")
@RequiredArgsConstructor
public class MessageRestController {

    private final MessageUseCase messageUseCase;

    @GetMapping("/{chatId}")
    public ResponseEntity<Page<MessageResponse>> getMessages(
            @PathVariable UUID chatId,
            @RequestParam int page,
            @RequestParam int size
    ) {
        return ResponseEntity.ok(messageUseCase.getMessages(chatId, page,  size));
    }
}
