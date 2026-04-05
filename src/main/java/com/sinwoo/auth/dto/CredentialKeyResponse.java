package com.sinwoo.auth.dto;

public record CredentialKeyResponse(
        String alg,
        String keyFormat,
        String publicKey
) {
}
