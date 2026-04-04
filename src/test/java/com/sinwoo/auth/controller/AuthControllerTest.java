package com.sinwoo.auth.controller;

import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.sinwoo.auth.dto.AuthProviderListResponse;
import com.sinwoo.auth.dto.AuthProviderResponse;
import com.sinwoo.auth.dto.CurrentUserResponse;
import com.sinwoo.auth.service.AuthService;
import com.sinwoo.common.accesslog.AccessLogService;
import com.sinwoo.common.security.AuthenticatedUser;
import com.sinwoo.common.security.SecurityConfig;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = AuthController.class)
@Import(SecurityConfig.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @MockBean
    private AccessLogService accessLogService;

    @Test
    void getOauthProvidersReturnsConfiguredProviders() throws Exception {
        given(authService.getOauthProviders()).willReturn(new AuthProviderListResponse(
                2,
                List.of(
                        new AuthProviderResponse("google", "Google", "/api/v1/auth/oauth/authorize/google"),
                        new AuthProviderResponse("microsoft", "Microsoft", "/api/v1/auth/oauth/authorize/microsoft")
                )
        ));

        mockMvc.perform(get("/api/v1/auth/oauth/providers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totCnt").value(2))
                .andExpect(jsonPath("$.itemList[0].registrationId").value("google"))
                .andExpect(jsonPath("$.itemList[1].registrationId").value("microsoft"));
    }

    @Test
    void getCurrentUserReturnsJwtPrincipalPayload() throws Exception {
        AuthenticatedUser authenticatedUser = new AuthenticatedUser(
                1L,
                100L,
                200L,
                "SINWOO.ADMIN",
                "admin@sinwoo.com",
                "Sinwoo Admin",
                "ADMIN",
                "OAUTH",
                List.of("ROLE_PLATFORM_ADMIN")
        );
        CurrentUserResponse response = new CurrentUserResponse(
                1L,
                100L,
                200L,
                "SINWOO.ADMIN",
                "admin@sinwoo.com",
                "Sinwoo Admin",
                "ADMIN",
                "OAUTH",
                List.of("ROLE_PLATFORM_ADMIN")
        );

        given(authService.getCurrentUser(authenticatedUser)).willReturn(response);

        mockMvc.perform(get("/api/v1/auth/me")
                        .with(authentication(new UsernamePasswordAuthenticationToken(
                                authenticatedUser,
                                "token",
                                List.of()
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.usrId").value(1))
                .andExpect(jsonPath("$.tenantId").value(100))
                .andExpect(jsonPath("$.roleCds[0]").value("ROLE_PLATFORM_ADMIN"));
    }
}
