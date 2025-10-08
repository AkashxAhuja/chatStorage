package com.example.chatstorage.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class RequestResponseLoggingFilter extends OncePerRequestFilter {

    private static final Logger requestLogger = LogManager.getLogger("REQUEST_LOG");
    private static final Logger responseLogger = LogManager.getLogger("RESPONSE_LOG");

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        ContentCachingRequestWrapper requestWrapper = wrapRequest(request);
        ContentCachingResponseWrapper responseWrapper = wrapResponse(response);
        long startTime = System.currentTimeMillis();
        try {
            filterChain.doFilter(requestWrapper, responseWrapper);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            logRequest(requestWrapper);
            logResponse(responseWrapper, duration);
            responseWrapper.copyBodyToResponse();
        }
    }

    private ContentCachingRequestWrapper wrapRequest(HttpServletRequest request) {
        if (request instanceof ContentCachingRequestWrapper wrapper) {
            return wrapper;
        }
        return new ContentCachingRequestWrapper(request);
    }

    private ContentCachingResponseWrapper wrapResponse(HttpServletResponse response) {
        if (response instanceof ContentCachingResponseWrapper wrapper) {
            return wrapper;
        }
        return new ContentCachingResponseWrapper(response);
    }

    private void logRequest(ContentCachingRequestWrapper request) {
        Map<String, String> headers = Collections.list(request.getHeaderNames()).stream()
                .collect(Collectors.toMap(name -> name, request::getHeader, (first, second) -> second, LinkedHashMap::new));

        String payload = getPayload(request.getContentAsByteArray(), request.getCharacterEncoding());
        String queryString = request.getQueryString();

        requestLogger.info("method={} uri={} query={} headers={} payload={}",
                request.getMethod(),
                request.getRequestURI(),
                queryString != null ? queryString : "",
                headers,
                payload);
    }

    private void logResponse(ContentCachingResponseWrapper response, long duration) {
        Map<String, String> headers = response.getHeaderNames().stream()
                .collect(Collectors.toMap(name -> name, response::getHeader, (first, second) -> second, LinkedHashMap::new));

        String payload = getPayload(response.getContentAsByteArray(), response.getCharacterEncoding());

        responseLogger.info("status={} durationMs={} headers={} payload={}",
                response.getStatus(),
                duration,
                headers,
                payload);
    }

    private String getPayload(byte[] content, String charsetName) {
        if (content == null || content.length == 0) {
            return "";
        }
        Charset charset = charsetName != null ? Charset.forName(charsetName) : StandardCharsets.UTF_8;
        return new String(content, charset);
    }
}
