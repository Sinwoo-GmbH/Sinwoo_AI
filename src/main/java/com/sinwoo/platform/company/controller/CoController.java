package com.sinwoo.platform.company.controller;

import com.sinwoo.platform.company.dto.CoListResponse;
import com.sinwoo.platform.company.dto.CoResponse;
import com.sinwoo.platform.company.dto.CreateCoRequest;
import com.sinwoo.platform.company.service.CoService;
import com.sinwoo.common.security.AuthenticatedUsr;
import com.sinwoo.common.exception.ApiException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/companies")
@RequiredArgsConstructor
public class CoController {

    private final CoService coService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CoResponse createCo(
            Authentication authentication,
            @Valid @RequestBody CreateCoRequest request
    ) {
        requirePlatformSuperAdmin(requireAuthenticatedUsr(authentication));
        return coService.createCo(request);
    }

    @GetMapping
    public CoListResponse getCos(
            Authentication authentication,
            @RequestParam(required = false) Long tenantId
    ) {
        AuthenticatedUsr authenticatedUsr = requireAuthenticatedUsr(authentication);
        Long resolvedTenantId = isPlatformSuperAdmin(authenticatedUsr) ? tenantId : authenticatedUsr.tenantId();
        if (resolvedTenantId == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "COMPANY_TENANT_REQUIRED", "Tenant is required.");
        }
        return coService.getCos(resolvedTenantId);
    }

    private AuthenticatedUsr requireAuthenticatedUsr(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedUsr authenticatedUsr)) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "AUTH_AUTHENTICATION_REQUIRED", "Authentication is required.");
        }
        return authenticatedUsr;
    }

    private void requirePlatformSuperAdmin(AuthenticatedUsr authenticatedUsr) {
        if (!isPlatformSuperAdmin(authenticatedUsr)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "COMPANY_CREATE_FORBIDDEN", "Only super admins can register customer cos.");
        }
    }

    private boolean isPlatformSuperAdmin(AuthenticatedUsr authenticatedUsr) {
        return authenticatedUsr.roleCds() != null && authenticatedUsr.roleCds().contains("PADM");
    }
}
