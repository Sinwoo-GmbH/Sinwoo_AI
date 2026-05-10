package com.sinwoo.common.exception;

import org.springframework.http.HttpStatus;

public class ApiException extends RuntimeException {

    private final HttpStatus status;
    private final String code;
    private final String clientMsg;

    public ApiException(HttpStatus status, String code, String clientMsg) {
        super(clientMsg);
        this.status = status;
        this.code = code;
        this.clientMsg = clientMsg;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getCd() {
        return code;
    }

    public String getClientMsg() {
        return clientMsg;
    }
}
