package com.sinwoo.tenant.controller;

import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sinwoo.common.accesslog.AccessLogService;
import com.sinwoo.common.security.SecurityConfig;
import com.sinwoo.common.web.SystemController;
import com.sinwoo.tenant.dto.CreateTenantRequest;
import com.sinwoo.tenant.dto.TenantListResponse;
import com.sinwoo.tenant.dto.TenantResponse;
import com.sinwoo.tenant.service.TenantService;
import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = {TenantController.class, SystemController.class})
@Import(SecurityConfig.class)
class TenantControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TenantService tenantService;

    @MockBean
    private AccessLogService accessLogService;

    @Test
    void createTenantReturnsCreatedTenant() throws Exception {
        OffsetDateTime now = OffsetDateTime.parse("2026-04-04T00:00:00Z");
        CreateTenantRequest request = new CreateTenantRequest("sinwoo", "Sinwoo", "sinwoo.com", "CUSTOMER", "N");
        TenantResponse response = new TenantResponse(1L, "SINWOO", "Sinwoo", "sinwoo.com", "CUSTOMER", "N", "ACTIVE", now, now);

        given(tenantService.createTenant(request)).willReturn(response);

        mockMvc.perform(post("/api/v1/tenants")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tenantId").value(1))
                .andExpect(jsonPath("$.tenantCd").value("SINWOO"))
                .andExpect(jsonPath("$.tenantNm").value("Sinwoo"))
                .andExpect(jsonPath("$.emlDomn").value("sinwoo.com"))
                .andExpect(jsonPath("$.tenantTpCd").value("CUSTOMER"))
                .andExpect(jsonPath("$.billFreeYn").value("N"))
                .andExpect(jsonPath("$.stsCd").value("ACTIVE"));
    }

    @Test
    void getTenantsReturnsAdminListResponse() throws Exception {
        OffsetDateTime now = OffsetDateTime.parse("2026-04-04T00:00:00Z");
        TenantListResponse response = new TenantListResponse(
                1,
                List.of(new TenantResponse(1L, "SINWOO", "Sinwoo", "sinwoo.com", "CUSTOMER", "N", "ACTIVE", now, now))
        );

        given(tenantService.getTenants()).willReturn(response);

        mockMvc.perform(get("/api/v1/tenants"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totCnt").value(1))
                .andExpect(jsonPath("$.itemList[0].tenantCd").value("SINWOO"))
                .andExpect(jsonPath("$.itemList[0].tenantNm").value("Sinwoo"))
                .andExpect(jsonPath("$.itemList[0].emlDomn").value("sinwoo.com"))
                .andExpect(jsonPath("$.itemList[0].tenantTpCd").value("CUSTOMER"))
                .andExpect(jsonPath("$.itemList[0].stsCd").value("ACTIVE"));
    }

    @Test
    void pingRemainsAccessible() throws Exception {
        mockMvc.perform(get("/api/v1/system/ping"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ok"))
                .andExpect(jsonPath("$.timestamp").exists());
    }
}
