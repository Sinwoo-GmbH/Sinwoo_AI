package com.sinwoo.common.security;

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

@Configuration
@EnableConfigurationProperties({SecurityProperties.class, AuthProperties.class})
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            ObjectProvider<JwtAuthenticationFilter> jwtAuthenticationFilterProvider,
            ObjectProvider<ClientRegistrationRepository> clientRegistrationRepositoryProvider,
            ObjectProvider<AuthenticationSuccessHandler> authenticationSuccessHandlerProvider,
            ObjectProvider<AuthenticationFailureHandler> authenticationFailureHandlerProvider
    ) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/actuator/health",
                                "/actuator/info",
                                "/api/v1/system/ping",
                                "/api/v1/auth/oauth/providers",
                                "/api/v1/auth/oauth/authorize/**",
                                "/api/v1/auth/oauth/failure",
                                "/oauth2/**",
                                "/login/oauth2/**"
                        ).permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/tenants").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/tenants").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/companies").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/companies").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/roles").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/roles").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/users").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/users").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/departments/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/departments/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/employees/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/employees/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/menus/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/menus/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/role-menu-auths/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/role-menu-auths/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/subscription-plans/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/subscription-plans/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/subscriptions/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/subscriptions/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/payment-transactions/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/payment-transactions/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/login").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/auth/oauth/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/oauth/**").permitAll()
                        .anyRequest().authenticated()
                )
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable);

        JwtAuthenticationFilter jwtAuthenticationFilter = jwtAuthenticationFilterProvider.getIfAvailable();
        if (jwtAuthenticationFilter != null) {
            http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
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
}
