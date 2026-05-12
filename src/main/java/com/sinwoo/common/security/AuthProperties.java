package com.sinwoo.common.security;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.auth")
public record AuthProperties(
        String frontendBaseUrl,
        String oauthCallbackPath,
        String customerDefaultRoleCd,
        String internalDefaultRoleCd,
        String defaultLoclCd,
        List<String> allowedOrigins
) {
}
