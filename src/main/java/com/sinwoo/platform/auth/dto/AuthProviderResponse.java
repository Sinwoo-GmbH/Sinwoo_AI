package com.sinwoo.platform.auth.dto;

public record AuthProviderResponse(
        String registrationId,
        String providerNm,
        String authorizeUri
) {
}
