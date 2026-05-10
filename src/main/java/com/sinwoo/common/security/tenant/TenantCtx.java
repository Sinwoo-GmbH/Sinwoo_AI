package com.sinwoo.common.security.tenant;

import java.util.Optional;

public final class TenantCtx {

    private static final ThreadLocal<String> CURRENT_TENANT = new ThreadLocal<>();

    private TenantCtx() {
    }

    public static void setTenantId(String tenantId) {
        CURRENT_TENANT.set(tenantId);
    }

    public static Optional<String> getTenantId() {
        return Optional.ofNullable(CURRENT_TENANT.get());
    }

    public static void clear() {
        CURRENT_TENANT.remove();
    }
}
