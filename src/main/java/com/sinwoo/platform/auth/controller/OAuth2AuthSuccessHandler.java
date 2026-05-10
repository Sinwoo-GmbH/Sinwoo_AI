package com.sinwoo.platform.auth.controller;

import com.sinwoo.platform.auth.dto.AuthTokenResponse;
import com.sinwoo.platform.auth.service.AuthService;
import com.sinwoo.common.security.AuthProperties;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Map;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class OAuth2AuthSuccessHandler implements AuthenticationSuccessHandler {

    private final AuthService authService;
    private final AuthProperties authProperties;

    public OAuth2AuthSuccessHandler(AuthService authService, AuthProperties authProperties) {
        this.authService = authService;
        this.authProperties = authProperties;
    }

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException, ServletException {
        if (!(authentication instanceof OAuth2AuthenticationToken oauth2AuthenticationToken)) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "OAuth2 authentication token is required");
            return;
        }

        HttpSession session = request.getSession(false);
        String tenantCd = session == null ? null : (String) session.getAttribute(AuthController.OAUTH_TENANT_SESSION_KEY);
        OAuth2User principal = oauth2AuthenticationToken.getPrincipal();

        AuthTokenResponse tokenResponse = authService.completeOauthLogin(
                oauth2AuthenticationToken.getAuthorizedClientRegistrationId(),
                tenantCd,
                principal.getAttributes()
        );

        if (session != null) {
            session.removeAttribute(AuthController.OAUTH_TENANT_SESSION_KEY);
        }

        String redirectUri = UriComponentsBuilder.fromUriString(authProperties.frontendBaseUrl())
                .path(authProperties.oauthCallbackPath())
                .queryParam("accessToken", tokenResponse.accessToken())
                .queryParam("refreshToken", tokenResponse.refreshToken())
                .queryParam("providerCd", tokenResponse.providerCd())
                .queryParam("lgnId", tokenResponse.user().lgnId())
                .queryParam("tenantCd", tokenResponse.user().tenantCd())
                .build(true)
                .toUriString();

        response.sendRedirect(redirectUri);
    }
}
