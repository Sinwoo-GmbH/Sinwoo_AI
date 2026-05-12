package com.sinwoo.common.security;

import com.sinwoo.common.security.tenant.TenantCtxFilt;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableConfigurationProperties({SecurityProperties.class, AuthProperties.class})
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            ObjectProvider<JwtAuthFilt> jwtAuthFiltProvider,
            ObjectProvider<TenantCtxFilt> tenantCtxFiltProvider,
            ObjectProvider<ClientRegistrationRepository> clientRegistrationRepositoryProvider,
            ObjectProvider<AuthenticationSuccessHandler> authenticationSuccessHandlerProvider,
            ObjectProvider<AuthenticationFailureHandler> authenticationFailureHandlerProvider
    ) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .authorizeHttpRequests(auth -> auth
                        // Infra / health
                        .requestMatchers(
                                "/actuator/health",
                                "/actuator/info",
                                "/error",
                                "/api/v1/system/ping"
                        ).permitAll()
                        // Auth endpoints (login, OAuth flow)
                        .requestMatchers(
                                "/api/v1/auth/credential-key",
                                "/api/v1/auth/oauth/providers",
                                "/api/v1/auth/oauth/authorize/**",
                                "/api/v1/auth/oauth/failure",
                                "/oauth2/**",
                                "/login/oauth2/**"
                        ).permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/login").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/auth/oauth/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/oauth/**").permitAll()
                        // Tenant signup (public registration entry point)
                        .requestMatchers(HttpMethod.POST, "/api/v1/tenants").permitAll()
                        // Everything else requires authentication
                        .anyRequest().authenticated()
                )
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable);

        JwtAuthFilt jwtAuthFilt = jwtAuthFiltProvider.getIfAvailable();
        if (jwtAuthFilt != null) {
            http.addFilterBefore(jwtAuthFilt, UsernamePasswordAuthenticationFilter.class);
            TenantCtxFilt tenantCtxFilt = tenantCtxFiltProvider.getIfAvailable();
            if (tenantCtxFilt != null) {
                http.addFilterAfter(tenantCtxFilt, JwtAuthFilt.class);
            }
        }

        if (clientRegistrationRepositoryProvider.getIfAvailable() != null) {
            http.oauth2Login(oauth2 -> {
                AuthenticationSuccessHandler successHandler = authenticationSuccessHandlerProvider.getIfAvailable();
                AuthenticationFailureHandler failureHandler = authenticationFailureHandlerProvider.getIfAvailable();
                if (successHandler != null) {
                    oauth2.successHandler(successHandler);
                }
                if (failureHandler != null) {
                    oauth2.failureHandler(failureHandler);
                }
            });
        }

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource(AuthProperties authProperties) {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowCredentials(true);
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setExposedHeaders(List.of("Authorization", "Location", "X-Request-Id"));

        Set<String> origins = new LinkedHashSet<>();
        if (authProperties.frontendBaseUrl() != null && !authProperties.frontendBaseUrl().isBlank()) {
            origins.add(authProperties.frontendBaseUrl().trim());
        }
        if (authProperties.allowedOrigins() != null) {
            for (String origin : authProperties.allowedOrigins()) {
                if (origin != null && !origin.isBlank()) {
                    origins.add(origin.trim());
                }
            }
        }
        configuration.setAllowedOrigins(new ArrayList<>(origins));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
