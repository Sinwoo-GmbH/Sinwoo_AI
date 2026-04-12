package com.sinwoo.company.controller;

import com.sinwoo.company.dto.CompanyListResponse;
import com.sinwoo.company.dto.CompanyResponse;
import com.sinwoo.company.dto.CreateCompanyRequest;
import com.sinwoo.company.service.CompanyService;
import com.sinwoo.common.security.AuthenticatedUser;
import com.sinwoo.common.web.ApiException;
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
public class CompanyController {

    private final CompanyService companyService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CompanyResponse createCompany(
            Authentication authentication,
            @Valid @RequestBody CreateCompanyRequest request
    ) {
        requirePlatformSuperAdmin(requireAuthenticatedUser(authentication));
        return companyService.createCompany(request);
    }

    @GetMapping
    public CompanyListResponse getCompanies(
            Authentication authentication,
            @RequestParam(required = false) Long tenantId
    ) {
        AuthenticatedUser authenticatedUser = requireAuthenticatedUser(authentication);
        Long resolvedTenantId = isPlatformSuperAdmin(authenticatedUser) ? tenantId : authenticatedUser.tenantId();
        if (resolvedTenantId == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "COMPANY_TENANT_REQUIRED", "Tenant is required.");
        }
        return companyService.getCompanies(resolvedTenantId);
    }

    private AuthenticatedUser requireAuthenticatedUser(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedUser authenticatedUser)) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "AUTH_AUTHENTICATION_REQUIRED", "Authentication is required.");
        }
        return authenticatedUser;
    }

    private void requirePlatformSuperAdmin(AuthenticatedUser authenticatedUser) {
        if (!isPlatformSuperAdmin(authenticatedUser)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "COMPANY_CREATE_FORBIDDEN", "Only super admins can register customer companies.");
        }
    }

    private boolean isPlatformSuperAdmin(AuthenticatedUser authenticatedUser) {
        return authenticatedUser.roleCds() != null && authenticatedUser.roleCds().contains("ROLE_PLATFORM_SUPER_ADMIN");
    }
}
