package com.sinwoo.common.accesslog;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

@Component
@Order(Ordered.LOWEST_PRECEDENCE - 100)
@RequiredArgsConstructor
public class AccessLogFilter extends OncePerRequestFilter {

    private static final int MAX_LOG_LENGTH = 4000;
    private static final int MAX_AUDIT_ACTOR_LENGTH = 100;
    public static final String REQUEST_ID_ATTRIBUTE = "sinwoo.requestId";

    private final AccessLogService accessLogService;
    private final ObjectMapper objectMapper;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return uri.startsWith("/actuator")
                || uri.startsWith("/error")
                || uri.startsWith("/favicon.ico")
                || uri.startsWith("/css/")
                || uri.startsWith("/js/")
                || uri.startsWith("/images/");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(response);

        String reqId = UUID.randomUUID().toString();
        wrappedRequest.setAttribute(REQUEST_ID_ATTRIBUTE, reqId);
        wrappedResponse.setHeader("X-Request-Id", reqId);
        MDC.put("reqId", reqId);

        Exception failure = null;
        try {
            filterChain.doFilter(wrappedRequest, wrappedResponse);
        } catch (Exception ex) {
            failure = ex;
            throw ex;
        } finally {
            try {
                accessLogService.write(buildEntry(reqId, wrappedRequest, wrappedResponse, failure));
            } catch (Exception ignored) {
                // Access log must never break the request lifecycle.
            } finally {
                MDC.remove("reqId");
                wrappedResponse.copyBodyToResponse();
            }
        }
    }

    private AccessLogEntry buildEntry(
            String reqId,
            ContentCachingRequestWrapper request,
            ContentCachingResponseWrapper response,
            Exception failure
    ) {
        OffsetDateTime now = OffsetDateTime.now();
        String usrKey = resolveUsrKey();
        String tenantKey = blankToNull(request.getHeader("X-Tenant-Id"));
        String errorMessage = failure == null ? null : truncate(failure.getMessage());

        return new AccessLogEntry(
                reqId,
                tenantKey,
                usrKey,
                request.getMethod(),
                request.getRequestURI(),
                truncate(request.getQueryString()),
                truncate(extractRequestBody(request)),
                truncate(extractHeaders(request)),
                truncate(resolveClientIp(request)),
                Integer.toString(response.getStatus()),
                isSuccess(response, failure) ? "Y" : "N",
                errorMessage,
                defaultActor(usrKey),
                now,
                defaultActor(usrKey),
                now
        );
    }

    private boolean isSuccess(ContentCachingResponseWrapper response, Exception failure) {
        return failure == null && response.getStatus() < 500;
    }

    private String resolveUsrKey() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null || authentication.getName().isBlank()) {
            return "ANON";
        }
        return authentication.getName();
    }

    private String resolveClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private String extractRequestBody(ContentCachingRequestWrapper request) {
        String contentType = request.getContentType();
        if (contentType != null && contentType.toLowerCase().startsWith("multipart/")) {
            return "[MULTIPART]";
        }
        if (request.getRequestURI().startsWith("/api/v1/auth")) {
            return "[MASKED]";
        }
        byte[] content = request.getContentAsByteArray();
        if (content.length == 0) {
            return null;
        }
        return new String(content, StandardCharsets.UTF_8);
    }

    private String extractHeaders(HttpServletRequest request) {
        Map<String, String> headers = new LinkedHashMap<>();
        Enumeration<String> names = request.getHeaderNames();
        while (names.hasMoreElements()) {
            String headerName = names.nextElement();
            String lowerName = headerName.toLowerCase();
            if ("authorization".equals(lowerName) || "cookie".equals(lowerName)) {
                headers.put(headerName, "[MASKED]");
            } else {
                headers.put(headerName, request.getHeader(headerName));
            }
        }

        try {
            return objectMapper.writeValueAsString(headers);
        } catch (JsonProcessingException ex) {
            return headers.toString();
        }
    }

    private String defaultActor(String usrKey) {
        String actor = usrKey == null || usrKey.isBlank() ? "SYSTEM" : usrKey;
        if (actor.length() <= MAX_AUDIT_ACTOR_LENGTH) {
            return actor;
        }
        return actor.substring(0, MAX_AUDIT_ACTOR_LENGTH);
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }

    private String truncate(String value) {
        if (value == null) {
            return null;
        }
        if (value.length() <= MAX_LOG_LENGTH) {
            return value;
        }
        return value.substring(0, MAX_LOG_LENGTH);
    }
}
