package com.sinwoo.common.security.tenant;

import com.sinwoo.common.security.AuthenticatedUsr;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.slf4j.MDC;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Populates {@link TenantCtx} and the {@code tenant} MDC key from the
 * JWT-authenticated principal. Must run after {@code JwtAuthFilt}.
 *
 * <p>Header-based tenant resolution was intentionally removed: the only trusted
 * source is the verified JWT claim.
 */
@Component
public class TenantCtxFilt extends OncePerRequestFilter {

    private static final String MDC_TENANT_KEY = "tenant";

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        boolean tenantSet = false;
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof AuthenticatedUsr usr
                    && usr.tenantId() != null) {
                TenantCtx.set(usr.tenantId(), usr.tenantCd());
                String mdcValue = usr.tenantCd() != null && !usr.tenantCd().isBlank()
                        ? usr.tenantCd()
                        : String.valueOf(usr.tenantId());
                MDC.put(MDC_TENANT_KEY, mdcValue);
                tenantSet = true;
            }
            filterChain.doFilter(request, response);
        } finally {
            if (tenantSet) {
                TenantCtx.clear();
                MDC.remove(MDC_TENANT_KEY);
            }
        }
    }
}
