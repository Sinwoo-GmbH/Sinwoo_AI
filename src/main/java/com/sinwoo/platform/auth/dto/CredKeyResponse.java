package com.sinwoo.platform.auth.dto;

public record CredKeyResponse(
        String alg,
        String keyFormat,
        String publicKey
) {
}
