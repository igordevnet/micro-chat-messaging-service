package com.microservice.microchatmessagingservice.controller;

import com.microservice.microchatmessagingservice.application.gateways.CacheGateway;
import com.microservice.microchatmessagingservice.domain.enums.Status;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/presence")
@RequiredArgsConstructor
@Tag(name = "Presence Management", description = "Endpoints for checking real-time online/offline status of users")
@SecurityRequirement(name = "bearerAuth")
public class PresenceController {

    private final CacheGateway cacheGateway;

    @Operation(summary = "Check user presence", description = "Retrieves the current online/offline status for a list of provided user IDs from the Redis cache. Used for initial frontend loading.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Presence status retrieved successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request (e.g., missing userIds parameter)", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Valid JWT required", content = @Content)
    })
    @GetMapping
    public ResponseEntity<Map<Long, Status>> checkPresence(
            @Parameter(description = "List of User IDs to check (comma separated)", required = true, example = "1,2,3")
            @RequestParam List<Long> userIds
    ) {
        return ResponseEntity.ok(cacheGateway.getUsersPresence(userIds));
    }
}