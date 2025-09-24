package com.example.chatstorage.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({SecurityProperties.class, RateLimitProperties.class, CorsProperties.class})
public class ApplicationConfig {
}
