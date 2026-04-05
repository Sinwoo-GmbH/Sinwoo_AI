package com.sinwoo.common.web;

import org.springframework.http.HttpStatus;

public class ApiException extends RuntimeException {

    private final HttpStatus status;
    private final String code;
    private final String clientMessage;

    public ApiException(HttpStatus status, String code, String clientMessage) {
        super(clientMessage);
        this.status = status;
        this.code = code;
        this.clientMessage = clientMessage;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getCode() {
        return code;
    }

    public String getClientMessage() {
        return clientMessage;
    }
}
