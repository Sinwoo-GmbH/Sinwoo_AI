package com.sinwoo.auth.support;

import org.springframework.http.HttpStatus;

public enum AuthErrorCode {
    AUTH_INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "Email address or password is incorrect."),
    AUTH_USER_INACTIVE(HttpStatus.FORBIDDEN, "Your account is not allowed to sign in right now."),
    AUTH_EMAIL_REQUIRED(HttpStatus.BAD_REQUEST, "Email address is required."),
    AUTH_TENANT_UNRESOLVED(HttpStatus.BAD_REQUEST, "Unable to identify your workspace from the email address."),
    AUTH_TENANT_AMBIGUOUS(HttpStatus.CONFLICT, "This email address matches more than one workspace. Please contact an administrator."),
    AUTH_AUTHENTICATION_REQUIRED(HttpStatus.UNAUTHORIZED, "Authentication is required."),
    AUTH_OAUTH_SUBJECT_MISSING(HttpStatus.BAD_REQUEST, "The external sign-in response is incomplete. Please try again."),
    AUTH_OAUTH_EMAIL_REQUIRED(HttpStatus.BAD_REQUEST, "The external account does not provide an email address."),
    AUTH_TENANT_NOT_FOUND(HttpStatus.BAD_REQUEST, "The requested workspace could not be found."),
    AUTH_DEFAULT_ROLE_NOT_FOUND(HttpStatus.BAD_REQUEST, "The default access role is not configured."),
    AUTH_LINKED_USER_NOT_FOUND(HttpStatus.BAD_REQUEST, "The linked account could not be found."),
    AUTH_ROLE_NOT_FOUND(HttpStatus.BAD_REQUEST, "The assigned role could not be found."),
    AUTH_PASSWORD_PAYLOAD_INVALID(HttpStatus.BAD_REQUEST, "The password payload is invalid."),
    AUTH_CREDENTIAL_KEY_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "The sign-in encryption key is not available.");

    private final HttpStatus status;
    private final String message;

    AuthErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }

    public HttpStatus status() {
        return status;
    }

    public String code() {
        return name();
    }

    public String message() {
        return message;
    }
}
