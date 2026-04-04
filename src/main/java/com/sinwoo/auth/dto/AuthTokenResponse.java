package com.sinwoo.auth.dto;

public record AuthTokenResponse(
        String accessToken,
        long accessTokenExpiresIn,
        String refreshToken,
        long refreshTokenExpiresIn,
        String tokenType,
        String providerCd,
        CurrentUserResponse user
) {
}
