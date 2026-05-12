package com.sinwoo.platform.support;

import com.sinwoo.platform.company.repository.CoRepository;
import com.sinwoo.platform.tenant.repository.TenantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

/**
 * Cross-cutting existence checks for platform-level references (tenant, company).
 * Each method throws {@link ResponseStatusException} with HTTP 400 on failure.
 */
@Component
@RequiredArgsConstructor
public class PlatformRefValidator {

    private final TenantRepository tenantRepository;
    private final CoRepository coRepository;

    /** Verify the tenant exists. Null or unknown tenant id raises 400. */
    public void requireTenantExists(Long tenantId) {
        if (tenantId == null || !tenantRepository.existsById(tenantId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tenant not found");
        }
    }

    /** Verify the company exists inside the given tenant. Null company id raises 400. */
    public void requireCoInTenant(Long tenantId, Long coId) {
        if (coId == null || coRepository.findByIdAndTenantId(coId, tenantId).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Co not found in tenant");
        }
    }

    /**
     * Verify the company exists inside the given tenant when {@code coId} is provided.
     * Null company id is allowed (no-op).
     */
    public void requireOptionalCoInTenant(Long tenantId, Long coId) {
        if (coId == null) {
            return;
        }
        if (coRepository.findByIdAndTenantId(coId, tenantId).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Co not found in tenant");
        }
    }
}
