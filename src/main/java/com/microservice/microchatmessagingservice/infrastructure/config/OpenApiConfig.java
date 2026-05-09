package com.microservice.microchatmessagingservice.infrastructure.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;

@OpenAPIDefinition(
        info = @Info(
                contact = @Contact(
                        name = "Igor Souza de Almeida",
                        email = "igorsouzaalmeida2404@gmail.com",
                        url = "https://github.com/igordevnet"
                ),
                description = "OpenApi documentation for Messaging-service (REST Endpoints)",
                title = "OpenApi specification - Messaging-service",
                version = "1.0"
        ),
        servers = {
                @Server(
                        description = "Local Standalone (Direct)",
                        url = "http://localhost:8082"
                ),
                @Server(
                        description = "Kubernetes Cluster (API Gateway)",
                        url = "http://api.microchat.local"
                )
        },
        security = {
                @SecurityRequirement(
                        name = "bearerAuth"
                )
        }
)
@SecurityScheme(
        name = "bearerAuth",
        description = "JWT auth description",
        scheme = "bearer",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        in = SecuritySchemeIn.HEADER
)
public class OpenApiConfig {
}