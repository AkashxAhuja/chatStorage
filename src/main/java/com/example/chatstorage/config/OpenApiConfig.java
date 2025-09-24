package com.example.chatstorage.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI chatStorageApi() {
        SecurityScheme apiKeyScheme = new SecurityScheme()
                .type(SecurityScheme.Type.APIKEY)
                .name("X-API-KEY")
                .in(SecurityScheme.In.HEADER);

        return new OpenAPI()
                .info(new Info().title("Chat Storage API").version("v1"))
                .components(new Components().addSecuritySchemes("apiKey", apiKeyScheme))
                .addSecurityItem(new SecurityRequirement().addList("apiKey"));
    }
}
