package com.example.chatstorage.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class RateLimitProperties {

    private final int requestsPerMinute;

    public RateLimitProperties(
            @Value("${RATE_LIMIT_REQUESTS_PER_MINUTE:${ratelimit.requests-per-minute:10}}") int requestsPerMinute) {
        this.requestsPerMinute = requestsPerMinute;
    }

    public int getRequestsPerMinute() {
        return requestsPerMinute;
    }
}
