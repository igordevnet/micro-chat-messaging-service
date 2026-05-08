package com.microservice.microchatmessagingservice.controller;

import com.microservice.microchatmessagingservice.application.usecases.MessageUseCase;
import com.microservice.microchatmessagingservice.controller.dtos.request.SendAudioRequest;
import com.microservice.microchatmessagingservice.controller.dtos.request.SendMessageRequest;
import com.microservice.microchatmessagingservice.controller.dtos.response.MessagePaginatedResponse;
import com.microservice.microchatmessagingservice.infrastructure.config.UserAuthenticated;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/api/message")
@RequiredArgsConstructor
@Tag(name = "Message Management", description = "REST Endpoints for retrieving messages and sending file/audio attachments")
@SecurityRequirement(name = "bearerAuth")
public class MessageRestController {

    private final MessageUseCase messageUseCase;

    @Operation(summary = "Get paginated messages", description = "Retrieves a paginated list of messages for a specific chat room. Automatically regenerates fresh presigned S3 URLs for attachments.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Messages retrieved successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = MessagePaginatedResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Valid JWT required", content = @Content),
            @ApiResponse(responseCode = "404", description = "Chat not found", content = @Content)
    })
    @GetMapping("/{chatId}")
    public ResponseEntity<MessagePaginatedResponse> getMessages(
            @Parameter(description = "UUID of the chat room", required = true) @PathVariable UUID chatId,
            @Parameter(description = "Page number (zero-based)", example = "0") @RequestParam int page,
            @Parameter(description = "Number of items per page", example = "20") @RequestParam int size
    ) {
        return ResponseEntity.ok(messageUseCase.getMessages(chatId, page,  size));
    }

    @Operation(summary = "Send a message (with optional file)", description = "Sends a text message or a file attachment. Must use multipart/form-data.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Message sent successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload or file size exceeded", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Valid JWT required", content = @Content)
    })
    @PostMapping(value = "/{chatId}/messages", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> sendMessage(
            @Parameter(description = "UUID of the chat room", required = true) @PathVariable UUID chatId,
            @Parameter(description = "Message metadata (JSON)", required = true) @RequestPart("data") SendMessageRequest request,
            @Parameter(description = "Optional file attachment (Image, PDF, etc.)") @RequestPart(value = "file", required = false) MultipartFile file,
            @Parameter(hidden = true) @AuthenticationPrincipal UserAuthenticated user
    ) {
        messageUseCase.saveMessage(chatId, user, request, file);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Operation(summary = "Send an audio message", description = "Uploads and sends an audio voice note to the chat. Must use multipart/form-data.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Audio message sent successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload or audio file missing", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Valid JWT required", content = @Content)
    })
    @PostMapping(value = "/{chatId}/audio", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> sendAudio(
            @Parameter(description = "UUID of the chat room", required = true) @PathVariable UUID chatId,
            @Parameter(description = "Audio metadata containing duration (JSON)", required = true) @RequestPart("data") SendAudioRequest request,
            @Parameter(description = "Audio file blob (.webm, .mp3, etc.)", required = true) @RequestPart("file") MultipartFile file,
            @Parameter(hidden = true) @AuthenticationPrincipal UserAuthenticated user
    ) {
        messageUseCase.saveAudioMessage(chatId, user, request, file);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}