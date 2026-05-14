package com.microservice.microchatmessagingservice.controller;

import com.microservice.microchatmessagingservice.application.usecases.FriendshipUseCase;
import com.microservice.microchatmessagingservice.controller.dtos.request.FriendshipAnswerRequest;
import com.microservice.microchatmessagingservice.controller.dtos.request.FriendshipRequest;
import com.microservice.microchatmessagingservice.controller.dtos.response.FriendshipResponse;
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
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/friendship")
@RequiredArgsConstructor
@Tag(name = "Friendship Management", description = "Endpoints for managing user friendships, requests, and blocking")
@SecurityRequirement(name = "bearerAuth")
public class FriendshipController {

    private final FriendshipUseCase friendshipUseCase;

    @Operation(summary = "Send a friendship request", description = "Sends a new friendship request to another user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Request sent successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Valid JWT required", content = @Content),
            @ApiResponse(responseCode = "409", description = "Conflict - Friendship request already exists", content = @Content)
    })
    @PostMapping("/request")
    public ResponseEntity<Void> sendRequest(
            @RequestBody FriendshipRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal UserAuthenticated user
    ) {
        friendshipUseCase.sendFriendshipRequest(request, user.id());
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Answer a friendship request", description = "Accept or decline a pending friendship request. Only the receiver can perform this action.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Request answered successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Valid JWT required", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden - Only the receiver can answer", content = @Content),
            @ApiResponse(responseCode = "404", description = "Friendship request not found", content = @Content)
    })
    @PutMapping("/answer")
    public ResponseEntity<Void> answerRequest(
            @RequestBody FriendshipAnswerRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal UserAuthenticated user
    ) {
        friendshipUseCase.answerFriendshipRequest(request, user.id());
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Block a friendship", description = "Blocks an existing friendship or pending request. Can be performed by either the sender or receiver.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Friendship blocked successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Valid JWT required", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden - User is not part of this friendship", content = @Content),
            @ApiResponse(responseCode = "404", description = "Friendship not found", content = @Content)
    })
    @PutMapping("/{friendshipId}/block")
    public ResponseEntity<Void> blockFriendship(
            @Parameter(description = "UUID of the friendship to block", required = true) @PathVariable UUID friendshipId,
            @Parameter(hidden = true) @AuthenticationPrincipal UserAuthenticated user
    ) {
        friendshipUseCase.blockFriendship(friendshipId, user.id());
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Get accepted friends", description = "Retrieves a list of User IDs for all accepted friendships of the current user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of friend IDs retrieved successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Long.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Valid JWT required", content = @Content)
    })
    @GetMapping("/friends")
    public ResponseEntity<List<Long>> getAcceptedFriends(
            @Parameter(hidden = true) @AuthenticationPrincipal UserAuthenticated user
    ) {
        List<Long> friends = friendshipUseCase.getAcceptedFriendIds(user.id());
        return ResponseEntity.ok(friends);
    }

    @Operation(summary = "Get pending friendship requests", description = "Retrieves a list of all pending friendship requests involving the authenticated user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pending requests retrieved successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = FriendshipResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Valid JWT required", content = @Content)
    })
    @GetMapping("/pending")
    public ResponseEntity<List<FriendshipResponse>> getPendingFriends(
            @Parameter(hidden = true) @AuthenticationPrincipal UserAuthenticated user
    ) {
        return ResponseEntity.ok(friendshipUseCase.getPendingFriend(user.id()));
    }

    @Operation(summary = "Get blocked friendships", description = "Retrieves a list of all friendships currently blocked by or affecting the authenticated user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Blocked friendships retrieved successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = FriendshipResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Valid JWT required", content = @Content)
    })
    @GetMapping("/blocked")
    public ResponseEntity<List<FriendshipResponse>> getBlockedFriends(
            @Parameter(hidden = true) @AuthenticationPrincipal UserAuthenticated user
    ) {
        return ResponseEntity.ok(friendshipUseCase.getBlockedFriend(user.id()));
    }
}