package com.microservice.microchatmessagingservice.controller.dtos.response;

public record AttachmentResponse(
        String fileName,
        String contentType,
        Long size,
        String url
) {}
