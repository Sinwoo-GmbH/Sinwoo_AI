package com.sinwoo.common.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.security")
public record SecurityProperties(
        String jwtSecret,
        long accessTokenTtlSeconds,
        long refreshTokenTtlSeconds
) {
}
