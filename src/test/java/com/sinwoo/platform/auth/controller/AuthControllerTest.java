package com.sinwoo.platform.auth.controller;

import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sinwoo.platform.auth.dto.AuthProviderListResponse;
import com.sinwoo.platform.auth.dto.AuthProviderResponse;
import com.sinwoo.platform.auth.dto.AuthTokenResponse;
import com.sinwoo.platform.auth.dto.CredKeyResponse;
import com.sinwoo.platform.auth.dto.CredLoginRequest;
import com.sinwoo.platform.auth.dto.CurrentUsrResponse;
import com.sinwoo.platform.auth.service.AuthService;
import com.sinwoo.common.audit.accesslog.AccessLogService;
import com.sinwoo.common.security.AuthenticatedUsr;
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

    @Autowired
    private ObjectMapper objectMapper;

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
    void getCredKeyReturnsPublicKeyPayload() throws Exception {
        given(authService.getCredKey()).willReturn(new CredKeyResponse(
                "RSA_OAEP_SHA256",
                "SPKI",
                "PUBLIC_KEY_BASE64"
        ));

        mockMvc.perform(get("/api/v1/auth/credential-key"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.alg").value("RSA_OAEP_SHA256"))
                .andExpect(jsonPath("$.keyFormat").value("SPKI"))
                .andExpect(jsonPath("$.publicKey").value("PUBLIC_KEY_BASE64"));
    }

    @Test
    void loginReturnsJwtPayload() throws Exception {
        CredLoginRequest request = new CredLoginRequest("admin@sinwoo.com", "encrypted-password");
        AuthTokenResponse response = new AuthTokenResponse(
                "access-token",
                3600,
                "refresh-token",
                1209600,
                "Bearer",
                "SINWOO",
                new CurrentUsrResponse(
                        1L,
                        100L,
                        "SINWOO_INTERNAL",
                        200L,
                        "INTERNAL",
                        "SINWOO.ADMIN",
                        "admin@sinwoo.com",
                        "Sinwoo Admin",
                        "ADMIN",
                        "PASSWORD",
                        List.of("ROLE_PLATFORM_ADMIN"),
                        "Y"
                )
        );

        given(authService.loginWithCreds(request)).willReturn(response);

        mockMvc.perform(post("/api/v1/auth/login")
                        .with(csrf())
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access-token"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-token"))
                .andExpect(jsonPath("$.providerCd").value("SINWOO"))
                .andExpect(jsonPath("$.user.lgnId").value("SINWOO.ADMIN"));
    }

    @Test
    void getCurrentUsrReturnsJwtPrincipalPayload() throws Exception {
        AuthenticatedUsr authenticatedUsr = new AuthenticatedUsr(
                1L,
                100L,
                "SINWOO_INTERNAL",
                200L,
                "INTERNAL",
                "SINWOO.ADMIN",
                "admin@sinwoo.com",
                "Sinwoo Admin",
                "ADMIN",
                "OAUTH",
                List.of("ROLE_PLATFORM_ADMIN")
        );
        CurrentUsrResponse response = new CurrentUsrResponse(
                1L,
                100L,
                "SINWOO_INTERNAL",
                200L,
                "INTERNAL",
                "SINWOO.ADMIN",
                "admin@sinwoo.com",
                "Sinwoo Admin",
                "ADMIN",
                "OAUTH",
                List.of("ROLE_PLATFORM_ADMIN"),
                "Y"
        );

        given(authService.getCurrentUsr(authenticatedUsr)).willReturn(response);

        mockMvc.perform(get("/api/v1/auth/me")
                        .with(authentication(new UsernamePasswordAuthenticationToken(
                                authenticatedUsr,
                                "token",
                                List.of()
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.usrId").value(1))
                .andExpect(jsonPath("$.tenantId").value(100))
                .andExpect(jsonPath("$.roleCds[0]").value("ROLE_PLATFORM_ADMIN"));
    }
}
