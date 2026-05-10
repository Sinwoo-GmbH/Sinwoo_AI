package com.sinwoo.platform.auth.controller;

import com.sinwoo.platform.auth.dto.AuthProviderListResponse;
import com.sinwoo.platform.auth.dto.AuthTokenResponse;
import com.sinwoo.platform.auth.dto.CredKeyResponse;
import com.sinwoo.platform.auth.dto.CredLoginRequest;
import com.sinwoo.platform.auth.dto.CurrentUsrResponse;
import com.sinwoo.platform.auth.support.AuthErrorCd;
import com.sinwoo.platform.auth.service.AuthService;
import com.sinwoo.common.security.AuthenticatedUsr;
import com.sinwoo.common.exception.ApiException;
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

    @GetMapping("/credential-key")
    public CredKeyResponse getCredKey() {
        return authService.getCredKey();
    }

    @PostMapping("/login")
    public AuthTokenResponse login(@Valid @RequestBody CredLoginRequest request) {
        return authService.loginWithCreds(request);
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
    public CurrentUsrResponse getCurrentUsr(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedUsr authenticatedUsr)) {
            throw new ApiException(
                    AuthErrorCd.AUTH_AUTHENTICATION_REQUIRED.status(),
                    AuthErrorCd.AUTH_AUTHENTICATION_REQUIRED.code(),
                    AuthErrorCd.AUTH_AUTHENTICATION_REQUIRED.message()
            );
        }
        return authService.getCurrentUsr(authenticatedUsr);
    }
}
