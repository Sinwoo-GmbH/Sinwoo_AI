package com.sinwoo.platform.auth.controller;

import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sinwoo.platform.auth.dto.CreateRoleRequest;
import com.sinwoo.platform.auth.dto.RoleListResponse;
import com.sinwoo.platform.auth.dto.RoleResponse;
import com.sinwoo.platform.auth.service.RoleService;
import com.sinwoo.common.audit.accesslog.AccessLogService;
import com.sinwoo.common.security.SecurityConfig;
import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = RoleController.class)
@Import(SecurityConfig.class)
class RoleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RoleService roleService;

    @MockBean
    private AccessLogService accessLogService;

    @Test
    void createRoleReturnsCreatedRole() throws Exception {
        OffsetDateTime now = OffsetDateTime.parse("2026-04-04T00:00:00Z");
        CreateRoleRequest request = new CreateRoleRequest("ROLE_FINANCE_MANAGER", "Finance Manager", "CUSTOMER", "CUSTOMER", "FINANCE_ADMIN", "TEAM_LEADER", "F", "7");
        RoleResponse response = new RoleResponse(10L, "ROLE_FINANCE_MANAGER", "Finance Manager", "CUSTOMER", "CUSTOMER", "FINANCE_ADMIN", "TEAM_LEADER", "F", "7", now, now);

        given(roleService.createRole(request)).willReturn(response);

        mockMvc.perform(post("/api/v1/roles")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.roleId").value(10))
                .andExpect(jsonPath("$.roleCd").value("ROLE_FINANCE_MANAGER"))
                .andExpect(jsonPath("$.roleNm").value("Finance Manager"))
                .andExpect(jsonPath("$.roleScopeCd").value("CUSTOMER"))
                .andExpect(jsonPath("$.roleD2Cd").value("FINANCE_ADMIN"))
                .andExpect(jsonPath("$.roleGrpCd").value("F"))
                .andExpect(jsonPath("$.roleLvlCd").value("7"));
    }

    @Test
    void getRolesReturnsAdminListResponse() throws Exception {
        OffsetDateTime now = OffsetDateTime.parse("2026-04-04T00:00:00Z");
        RoleListResponse response = new RoleListResponse(
                2,
                List.of(
                        new RoleResponse(1L, "ROLE_PLATFORM_SUPER_ADMIN", "Platform Super Admin", "ADMIN", "ADMIN", "SUPER_ADMIN", "ROOT", "A", "9", now, now),
                        new RoleResponse(3L, "ROLE_CUSTOMER_FINANCE_ADMIN_LEADER", "Customer Finance Admin Leader", "CUSTOMER", "CUSTOMER", "FINANCE_ADMIN", "TEAM_LEADER", "F", "5", now, now)
                )
        );

        given(roleService.getRoles()).willReturn(response);

        mockMvc.perform(get("/api/v1/roles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totCnt").value(2))
                .andExpect(jsonPath("$.itemList[0].roleCd").value("ROLE_PLATFORM_SUPER_ADMIN"))
                .andExpect(jsonPath("$.itemList[1].roleCd").value("ROLE_CUSTOMER_FINANCE_ADMIN_LEADER"));
    }
}
