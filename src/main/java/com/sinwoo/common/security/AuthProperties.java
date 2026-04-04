package com.sinwoo.common.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.auth")
public record AuthProperties(
        String frontendBaseUrl,
        String oauthCallbackPath,
        String customerDefaultRoleCd,
        String internalDefaultRoleCd,
        String defaultLoclCd
) {
}
