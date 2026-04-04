package com.sinwoo.auth.controller;

import com.sinwoo.common.security.AuthProperties;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class OAuth2AuthenticationFailureHandler implements AuthenticationFailureHandler {

    private final AuthProperties authProperties;

    public OAuth2AuthenticationFailureHandler(AuthProperties authProperties) {
        this.authProperties = authProperties;
    }

    @Override
    public void onAuthenticationFailure(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException exception
    ) throws IOException, ServletException {
        String redirectUri = UriComponentsBuilder.fromUriString(authProperties.frontendBaseUrl())
                .path(authProperties.oauthCallbackPath())
                .queryParam("error", exception.getMessage())
                .build(true)
                .toUriString();

        response.sendRedirect(redirectUri);
    }
}
