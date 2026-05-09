package com.microservice.microchatmessagingservice.controller;

import com.microservice.microchatmessagingservice.application.usecases.ChatUseCase;
import com.microservice.microchatmessagingservice.controller.dtos.response.ChatResponse;
import com.microservice.microchatmessagingservice.controller.dtos.request.ChatRequest;
import com.microservice.microchatmessagingservice.infrastructure.config.UserAuthenticated;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@Tag(name = "Chat Management", description = "Endpoints for creating, updating, and managing user chat rooms")
@SecurityRequirement(name = "bearerAuth")
public class ChatController {

    private final ChatUseCase chatUseCase;

    @Operation(summary = "Create a new chat", description = "Creates a new private or group chat and adds the creator as a participant.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Chat created successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ChatResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request payload", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Valid JWT required", content = @Content)
    })
    @PostMapping
    public ResponseEntity<ChatResponse> createChat(
            @RequestBody ChatRequest chatRequest,
            @Parameter(hidden = true) @AuthenticationPrincipal UserAuthenticated currentUser
    ) {
        Long userId = currentUser.id();
        return ResponseEntity.status(HttpStatus.CREATED).body(chatUseCase.createChat(chatRequest, userId));
    }

    @Operation(summary = "Get user's chats", description = "Retrieves a list of all chats the authenticated user is currently participating in.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Chats retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Valid JWT required", content = @Content)
    })
    @GetMapping("/user")
    public ResponseEntity<List<ChatResponse>> getChats(
            @Parameter(hidden = true) @AuthenticationPrincipal UserAuthenticated currentUser
    ) {
        Long userId = currentUser.id();
        return ResponseEntity.ok(chatUseCase.getAllChats(userId));
    }

    @Operation(summary = "Update chat details", description = "Updates the name or configuration of an existing chat room.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Chat updated successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ChatResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request payload", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Valid JWT required", content = @Content),
            @ApiResponse(responseCode = "404", description = "Chat not found", content = @Content)
    })
    @PatchMapping("/{chatId}")
    public ResponseEntity<ChatResponse> updateChat(
            @Parameter(description = "UUID of the chat to update", required = true) @PathVariable UUID chatId,
            @RequestBody ChatRequest chatRequest
    ) {
        return ResponseEntity.ok(chatUseCase.updateChat(chatId, chatRequest));
    }

    @Operation(summary = "Delete a chat", description = "Deletes a chat room. The user must be a participant of the chat to delete it.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Chat deleted successfully (or user handled gracefully)"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Valid JWT required", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden - User is not a participant of the chat", content = @Content)
    })
    @DeleteMapping("/{chatId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteChat(
            @Parameter(description = "UUID of the chat to delete", required = true) @PathVariable UUID chatId,
            @Parameter(hidden = true) @AuthenticationPrincipal UserAuthenticated currentUser
    ) {
        Long userId = currentUser.id();
        chatUseCase.deleteChat(userId, chatId);
    }
}