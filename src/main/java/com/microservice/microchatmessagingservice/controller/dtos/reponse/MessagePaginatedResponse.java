package com.microservice.microchatmessagingservice.controller.dtos.reponse;

import java.util.List;

public record MessagePaginatedResponse(
        List<MessageResponse> content,
        int currentPage,
        int totalPages,
        long totalElements
) {}
