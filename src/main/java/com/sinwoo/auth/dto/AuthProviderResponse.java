package com.sinwoo.auth.dto;

public record AuthProviderResponse(
        String registrationId,
        String providerNm,
        String authorizeUri
) {
}
