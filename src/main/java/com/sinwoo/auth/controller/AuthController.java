package com.sinwoo.auth.controller;

import com.sinwoo.auth.dto.AuthProviderListResponse;
import com.sinwoo.auth.dto.AuthTokenResponse;
import com.sinwoo.auth.dto.CredentialLoginRequest;
import com.sinwoo.auth.dto.CurrentUserResponse;
import com.sinwoo.auth.service.AuthService;
import com.sinwoo.common.security.AuthenticatedUser;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    public static final String OAUTH_TENANT_SESSION_KEY = "SINWOO_OAUTH_TENANT_CD";

    private final AuthService authService;

    @GetMapping("/oauth/providers")
    public AuthProviderListResponse getOauthProviders() {
        return authService.getOauthProviders();
    }

    @PostMapping("/login")
    public AuthTokenResponse login(@Valid @RequestBody CredentialLoginRequest request) {
        return authService.loginWithCredentials(request);
    }

    @GetMapping("/oauth/authorize/{registrationId}")
    public ResponseEntity<Void> authorize(
            @PathVariable String registrationId,
            @RequestParam(required = false) String tenantCd,
            HttpServletRequest request
    ) {
        if (tenantCd != null && !tenantCd.isBlank()) {
            HttpSession session = request.getSession(true);
            session.setAttribute(OAUTH_TENANT_SESSION_KEY, tenantCd.trim().toUpperCase());
        }

        return ResponseEntity.status(302)
                .header(HttpHeaders.LOCATION, "/oauth2/authorization/" + registrationId)
                .build();
    }

    @GetMapping("/me")
    public CurrentUserResponse getCurrentUser(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedUser authenticatedUser)) {
            throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.UNAUTHORIZED, "Authentication required");
        }
        return authService.getCurrentUser(authenticatedUser);
    }
}
