package com.sinwoo.platform.mnu.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sinwoo.common.audit.accesslog.AccessLogService;
import com.sinwoo.common.security.AuthenticatedUsr;
import com.sinwoo.common.security.SecurityConfig;
import com.sinwoo.platform.mnu.dto.CreateMnuRequest;
import com.sinwoo.platform.mnu.dto.MnuListResponse;
import com.sinwoo.platform.mnu.dto.MnuNodeResponse;
import com.sinwoo.platform.mnu.dto.MnuResponse;
import com.sinwoo.platform.mnu.dto.MnuTreeResponse;
import com.sinwoo.platform.mnu.service.MnuService;
import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;

@WebMvcTest(controllers = MnuController.class)
@Import(SecurityConfig.class)
class MnuControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MnuController mnuController;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private MnuService mnuService;

    @MockBean
    private AccessLogService accessLogService;

    @Test
    void createMnuReturnsCreatedMnu() throws Exception {
        OffsetDateTime now = OffsetDateTime.parse("2026-04-04T00:00:00Z");
        CreateMnuRequest request = new CreateMnuRequest("MNU_ADMIN_DASH", "MNU_ADMIN_DASH", "Admin Dashboard", "ADMIN", null, "/admin/dashboard", "layout-dashboard", 10, null, "Y");
        MnuResponse response = new MnuResponse(1L, "MNU_ADMIN_DASH", "MNU_ADMIN_DASH", "Admin Dashboard", "ADMIN", null, "/admin/dashboard", "layout-dashboard", 10, null, "Y", now, now);

        given(mnuService.createMnu(request)).willReturn(response);

        mockMvc.perform(post("/api/v1/menus")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.mnuCd").value("MNU_ADMIN_DASH"))
                .andExpect(jsonPath("$.mnuScopeCd").value("ADMIN"))
                .andExpect(jsonPath("$.useYn").value("Y"));
    }

    @Test
    void getVisibleMnusReturnsMnuTree() throws Exception {
        MnuTreeResponse response = new MnuTreeResponse(
                1,
                List.of(new MnuNodeResponse(1L, "MNU_CUSTOMER_DASH", "MNU_CUSTOMER_DASH", "Customer Dashboard", "CUSTOMER", null, "/customer/dashboard", "layout-dashboard", 10, null, List.of()))
        );

        given(mnuService.getVisibleMnus(List.of("ROLE_CUSTOMER_USER_MEMBER"), "CUSTOMER")).willReturn(response);

        mockMvc.perform(get("/api/v1/menus/visible")
                        .param("roleCd", "ROLE_CUSTOMER_USER_MEMBER")
                        .param("mnuScopeCd", "CUSTOMER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totCnt").value(1))
                .andExpect(jsonPath("$.itemList[0].mnuCd").value("MNU_CUSTOMER_DASH"))
                .andExpect(jsonPath("$.itemList[0].mnuScopeCd").value("CUSTOMER"));
    }

    @Test
    void getVisibleMnusByUsrReturnsMnuTree() throws Exception {
        MnuTreeResponse response = new MnuTreeResponse(
                1,
                List.of(new MnuNodeResponse(5L, "MNU_CUSTOMER_FIN", "MNU_CUSTOMER_FIN", "Finance Management", "CUSTOMER", null, "/customer/finance", "wallet", 40, "PAID_CUSTOMER_ADMIN", List.of()))
        );

        given(mnuService.getVisibleMnusByUsr(100L, "CUSTOMER")).willReturn(response);

        mockMvc.perform(get("/api/v1/menus/visible-by-user")
                        .param("usrId", "100")
                        .param("mnuScopeCd", "CUSTOMER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totCnt").value(1))
                .andExpect(jsonPath("$.itemList[0].mnuCd").value("MNU_CUSTOMER_FIN"));
    }

    @Test
    void getMnusReturnsFlatList() throws Exception {
        OffsetDateTime now = OffsetDateTime.parse("2026-04-04T00:00:00Z");
        MnuListResponse response = new MnuListResponse(
                1,
                List.of(new MnuResponse(1L, "MNU_ADMIN_DASH", "MNU_ADMIN_DASH", "Admin Dashboard", "ADMIN", null, "/admin/dashboard", "layout-dashboard", 10, null, "Y", now, now))
        );

        given(mnuService.getMnus("ADMIN")).willReturn(response);

        mockMvc.perform(get("/api/v1/menus").param("mnuScopeCd", "ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totCnt").value(1))
                .andExpect(jsonPath("$.itemList[0].mnuCd").value("MNU_ADMIN_DASH"));
    }

    @Test
    void getVisibleMnusForCurrentUsrReturnsMnuTree() throws Exception {
        AuthenticatedUsr authenticatedUsr = new AuthenticatedUsr(
                1L,
                100L,
                "SINWOO_INTERNAL",
                200L,
                "CUSTOMER",
                "GGAMGANG",
                "ggamgang@sinwoo-itc.com",
                "Sinwoo Admin",
                "CUSTOMER",
                "PASSWORD",
                List.of("ROLE_CUSTOMER_ADMIN_MEMBER")
        );
        MnuTreeResponse response = new MnuTreeResponse(
                1,
                List.of(new MnuNodeResponse(5L, "MNU_CUSTOMER_PAY", "MNU_CUSTOMER_PAY", "Payment Center", "CUSTOMER", null, "/customer/payments", "receipt", 50, null, List.of()))
        );

        given(mnuService.getVisibleMnusForCurrentUsr(authenticatedUsr, "CUSTOMER")).willReturn(response);

        mockMvc.perform(get("/api/v1/menus/my")
                        .param("mnuScopeCd", "CUSTOMER")
                        .with(authentication(new UsernamePasswordAuthenticationToken(authenticatedUsr, "token", List.of()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totCnt").value(1))
                .andExpect(jsonPath("$.itemList[0].mnuCd").value("MNU_CUSTOMER_PAY"));
    }

    @Test
    void getVisibleMnusForCurrentUsrAppliesRequestedLangToLocaleCtx() throws Exception {
        AuthenticatedUsr authenticatedUsr = new AuthenticatedUsr(
                1L,
                100L,
                "SINWOO_INTERNAL",
                200L,
                "CUSTOMER",
                "GGAMGANG",
                "ggamgang@sinwoo-itc.com",
                "Sinwoo Admin",
                "CUSTOMER",
                "PASSWORD",
                List.of("ROLE_CUSTOMER_ADMIN_MEMBER")
        );
        MnuTreeResponse response = new MnuTreeResponse(
                1,
                List.of(new MnuNodeResponse(5L, "MNU_CUSTOMER_PAY", "MNU_CUSTOMER_PAY", "Zahlungscenter", "CUSTOMER", null, "/customer/payments", "receipt", 50, null, List.of()))
        );

        given(mnuService.getVisibleMnusForCurrentUsr(authenticatedUsr, "CUSTOMER")).willAnswer(invocation -> {
            assertThat(LocaleContextHolder.getLocale().getLanguage()).isEqualTo("de");
            return response;
        });

        MnuTreeResponse actual = mnuController.getVisibleMnusForCurrentUsr(
                new UsernamePasswordAuthenticationToken(authenticatedUsr, "token", List.of()),
                "CUSTOMER",
                "de"
        );

        assertThat(actual.itemList()).hasSize(1);
        assertThat(actual.itemList().getFirst().mnuNm()).isEqualTo("Zahlungscenter");
    }
}
