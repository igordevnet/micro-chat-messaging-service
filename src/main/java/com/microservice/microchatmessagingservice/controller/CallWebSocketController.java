package com.microservice.microchatmessagingservice.controller;

import com.microservice.microchatmessagingservice.application.usecases.CallUseCase;
import com.microservice.microchatmessagingservice.controller.dtos.request.SignalingRequest;
import com.microservice.microchatmessagingservice.infrastructure.config.UserAuthenticated;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class CallWebSocketController {

    private final CallUseCase callUseCase;

    @MessageMapping("/call/signaling")
    public void handleSignaling(
            @Payload SignalingRequest request,
            Principal principal
    ) {
        var auth = (UsernamePasswordAuthenticationToken) principal;
        var currentUser = (UserAuthenticated) auth.getPrincipal();

        callUseCase.handleCall(request, currentUser.id());
    }
}
