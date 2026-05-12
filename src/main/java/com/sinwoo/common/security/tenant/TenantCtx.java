package com.sinwoo.common.security.tenant;

import java.util.Optional;

/**
 * Holds the current tenant identifiers resolved from the authenticated user (JWT claims).
 * Populated by {@link TenantCtxFilt}; cleared at the end of each request.
 *
 * <p>{@code tenantId} (Long) is used for FK lookups in queries.
 * {@code tenantCd} (String, e.g. "SINWOO") is used for display, logs, and audit.
 */
public final class TenantCtx {

    private static final ThreadLocal<Long> CURRENT_TENANT_ID = new ThreadLocal<>();
    private static final ThreadLocal<String> CURRENT_TENANT_CD = new ThreadLocal<>();

    private TenantCtx() {
    }

    public static void set(Long tenantId, String tenantCd) {
        CURRENT_TENANT_ID.set(tenantId);
        CURRENT_TENANT_CD.set(tenantCd);
    }

    public static Optional<Long> getTenantId() {
        return Optional.ofNullable(CURRENT_TENANT_ID.get());
    }

    public static Optional<String> getTenantCd() {
        return Optional.ofNullable(CURRENT_TENANT_CD.get());
    }

    public static void clear() {
        CURRENT_TENANT_ID.remove();
        CURRENT_TENANT_CD.remove();
    }
}
