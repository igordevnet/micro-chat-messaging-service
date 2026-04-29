package com.microservice.microchatmessagingservice.controller;

import com.microservice.microchatmessagingservice.application.gateways.RedisPresenceGateway;
import com.microservice.microchatmessagingservice.domain.enums.Status;
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
public class PresenceController {

    private final RedisPresenceGateway redisPresenceGateway;

    @GetMapping
    public ResponseEntity<Map<Long, Status>> checkPresence(@RequestParam List<Long> userIds) {
        return ResponseEntity.ok(redisPresenceGateway.getUsersPresence(userIds));
    }
}
