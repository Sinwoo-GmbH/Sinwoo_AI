package com.sinwoo.common.config;

import com.sinwoo.common.security.AuthenticatedUsr;
import java.util.Optional;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Provides the current actor name to {@code @CreatedBy} / {@code @LastModifiedBy}
 * fields on JPA auditing entities.
 *
 * <p>Order of resolution: authenticated user's login id, then email, then "SYSTEM".
 */
@Configuration
public class AuditorAwareConfig {

    private static final String SYSTEM_ACTOR = "SYSTEM";

    @Bean
    public AuditorAware<String> auditorProvider() {
        return () -> Optional.of(resolveCurrentActor());
    }

    private String resolveCurrentActor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedUsr usr)) {
            return SYSTEM_ACTOR;
        }
        String key = usr.resolveKey();
        return key == null ? SYSTEM_ACTOR : key;
    }
}
