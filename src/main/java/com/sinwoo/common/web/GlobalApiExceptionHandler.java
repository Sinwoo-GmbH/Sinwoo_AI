package com.sinwoo.common.web;

import com.sinwoo.common.accesslog.AccessLogFilter;
import com.sinwoo.common.security.AuthenticatedUser;
import jakarta.servlet.http.HttpServletRequest;
import java.time.OffsetDateTime;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
@Slf4j
public class GlobalApiExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiErrorResponse> handleApiException(ApiException exception, HttpServletRequest request) {
        logException(exception.getStatus(), exception, request);
        return ResponseEntity.status(exception.getStatus())
                .body(buildResponse(
                        exception.getStatus(),
                        exception.getCode(),
                        exception.getClientMessage(),
                        request.getRequestURI()
                ));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidationException(
            MethodArgumentNotValidException exception,
            HttpServletRequest request
    ) {
        String message = resolveValidationMessage(exception.getBindingResult().getFieldErrors());
        logException(HttpStatus.BAD_REQUEST, exception, request);
        return ResponseEntity.badRequest()
                .body(buildResponse(
                        HttpStatus.BAD_REQUEST,
                        "REQUEST_VALIDATION_ERROR",
                        message,
                        request.getRequestURI()
                ));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleUnreadableBodyException(
            HttpMessageNotReadableException exception,
            HttpServletRequest request
    ) {
        logException(HttpStatus.BAD_REQUEST, exception, request);
        return ResponseEntity.badRequest()
                .body(buildResponse(
                        HttpStatus.BAD_REQUEST,
                        "REQUEST_BODY_INVALID",
                        "Please check the request data and try again.",
                        request.getRequestURI()
                ));
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiErrorResponse> handleResponseStatusException(
            ResponseStatusException exception,
            HttpServletRequest request
    ) {
        HttpStatus status = HttpStatus.valueOf(exception.getStatusCode().value());
        logException(status, exception, request);
        return ResponseEntity.status(status)
                .body(buildResponse(
                        status,
                        "REQUEST_FAILED",
                        resolveReasonOrDefault(status, exception.getReason()),
                        request.getRequestURI()
                ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnexpectedException(Exception exception, HttpServletRequest request) {
        logException(HttpStatus.INTERNAL_SERVER_ERROR, exception, request);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(buildResponse(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "INTERNAL_SERVER_ERROR",
                        "An unexpected error occurred. Please try again later.",
                        request.getRequestURI()
                ));
    }

    private ApiErrorResponse buildResponse(HttpStatus status, String code, String message, String path) {
        return new ApiErrorResponse(
                OffsetDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                code,
                message,
                path
        );
    }

    private String resolveValidationMessage(List<FieldError> fieldErrors) {
        for (FieldError fieldError : fieldErrors) {
            if ("eml".equals(fieldError.getField())) {
                return "Please enter a valid email address.";
            }
            if ("pwd".equals(fieldError.getField())) {
                return "Please check your password and try again.";
            }
        }
        return "Please check the required fields and try again.";
    }

    private void logException(HttpStatus status, Exception exception, HttpServletRequest request) {
        String requestContext = buildRequestContext(request);

        if (status.is5xxServerError()) {
            log.error("Unhandled API error: {}", requestContext, exception);
            return;
        }

        log.warn("API request failed: {}", requestContext, exception);
    }

    private String resolveReasonOrDefault(HttpStatus status, String reason) {
        if (reason != null && !reason.isBlank()) {
            return reason;
        }
        return switch (status) {
            case UNAUTHORIZED -> "Authentication is required.";
            case FORBIDDEN -> "You do not have permission to perform this action.";
            case BAD_REQUEST -> "The request could not be processed.";
            case CONFLICT -> "The request could not be completed due to a conflict.";
            default -> "The request could not be completed.";
        };
    }

    private String buildRequestContext(HttpServletRequest request) {
        String method = request.getMethod();
        String path = request.getRequestURI();
        String query = request.getQueryString();
        Object requestId = request.getAttribute(AccessLogFilter.REQUEST_ID_ATTRIBUTE);
        Long authenticatedUserId = resolveAuthenticatedUserId();

        return "requestId=%s method=%s path=%s query=%s userId=%s".formatted(
                valueOrDash(requestId),
                valueOrDash(method),
                valueOrDash(path),
                valueOrDash(query),
                authenticatedUserId == null ? "-" : authenticatedUserId
        );
    }

    private Long resolveAuthenticatedUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedUser authenticatedUser)) {
            return null;
        }
        return authenticatedUser.usrId();
    }

    private String valueOrDash(Object value) {
        if (value == null) {
            return "-";
        }
        String text = String.valueOf(value);
        return text.isBlank() ? "-" : text;
    }
}
