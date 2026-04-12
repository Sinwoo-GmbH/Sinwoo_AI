package com.sinwoo.company.controller;

import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sinwoo.common.accesslog.AccessLogService;
import com.sinwoo.common.security.AuthenticatedUser;
import com.sinwoo.common.security.SecurityConfig;
import com.sinwoo.company.dto.CompanyListResponse;
import com.sinwoo.company.dto.CompanyResponse;
import com.sinwoo.company.dto.CreateCompanyRequest;
import com.sinwoo.company.service.CompanyService;
import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;

@WebMvcTest(controllers = CompanyController.class)
@Import(SecurityConfig.class)
class CompanyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CompanyService companyService;

    @MockBean
    private AccessLogService accessLogService;

    @Test
    void createCompanyReturnsCreatedCompany() throws Exception {
        OffsetDateTime now = OffsetDateTime.parse("2026-04-04T00:00:00Z");
        AuthenticatedUser authenticatedUser = new AuthenticatedUser(
                1L,
                2L,
                10L,
                "INTERNAL",
                "GGAMGANG",
                "ggamgang@sinwoo-itc.com",
                "Super Admin",
                "PLATFORM",
                "SUPER",
                List.of("ROLE_PLATFORM_SUPER_ADMIN")
        );
        CreateCompanyRequest request = new CreateCompanyRequest(
                2L,
                "SINWOO_DE",
                "Sinwoo Germany",
                "DE-REG-0001",
                "DE",
                "HE",
                "Frankfurt am Main",
                "Mainzer Landstrasse 10",
                "ACTIVE"
        );
        CompanyResponse response = new CompanyResponse(
                1L,
                2L,
                "SINWOO_DE",
                "Sinwoo Germany",
                "DE-REG-0001",
                "DE",
                "HE",
                "Frankfurt am Main",
                "Mainzer Landstrasse 10",
                "ACTIVE",
                now,
                now
        );

        given(companyService.createCompany(request)).willReturn(response);

        mockMvc.perform(post("/api/v1/companies")
                        .with(csrf())
                        .with(authentication(new UsernamePasswordAuthenticationToken(authenticatedUser, "token", List.of())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.coId").value(1))
                .andExpect(jsonPath("$.tenantId").value(2))
                .andExpect(jsonPath("$.coCd").value("SINWOO_DE"))
                .andExpect(jsonPath("$.coNm").value("Sinwoo Germany"))
                .andExpect(jsonPath("$.regNo").value("DE-REG-0001"))
                .andExpect(jsonPath("$.stsCd").value("ACTIVE"));
    }

    @Test
    void getCompaniesReturnsAdminListResponse() throws Exception {
        OffsetDateTime now = OffsetDateTime.parse("2026-04-04T00:00:00Z");
        AuthenticatedUser authenticatedUser = new AuthenticatedUser(
                1L,
                2L,
                10L,
                "INTERNAL",
                "GGAMGANG",
                "ggamgang@sinwoo-itc.com",
                "Super Admin",
                "PLATFORM",
                "SUPER",
                List.of("ROLE_PLATFORM_SUPER_ADMIN")
        );
        CompanyListResponse response = new CompanyListResponse(
                1,
                List.of(new CompanyResponse(
                        1L,
                        2L,
                        "SINWOO_DE",
                        "Sinwoo Germany",
                        "DE-REG-0001",
                        "DE",
                        "HE",
                        "Frankfurt am Main",
                        "Mainzer Landstrasse 10",
                        "ACTIVE",
                        now,
                        now
                ))
        );

        given(companyService.getCompanies(2L)).willReturn(response);

        mockMvc.perform(get("/api/v1/companies")
                        .param("tenantId", "2")
                        .with(authentication(new UsernamePasswordAuthenticationToken(authenticatedUser, "token", List.of()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totCnt").value(1))
                .andExpect(jsonPath("$.itemList[0].coCd").value("SINWOO_DE"))
                .andExpect(jsonPath("$.itemList[0].coNm").value("Sinwoo Germany"))
                .andExpect(jsonPath("$.itemList[0].stsCd").value("ACTIVE"));
    }
}
