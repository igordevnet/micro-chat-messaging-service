package com.microservice.microchatmessagingservice.controller;

import com.microservice.microchatmessagingservice.application.usecases.MessageUseCase;
import com.microservice.microchatmessagingservice.controller.dtos.response.MessagePaginatedResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
}
