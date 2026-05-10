package com.sinwoo.platform.emp.controller;

import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sinwoo.common.audit.accesslog.AccessLogService;
import com.sinwoo.common.security.SecurityConfig;
import com.sinwoo.platform.emp.dto.CreateEmpRequest;
import com.sinwoo.platform.emp.dto.EmpListResponse;
import com.sinwoo.platform.emp.dto.EmpResponse;
import com.sinwoo.platform.emp.service.EmpService;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = EmpController.class)
@Import(SecurityConfig.class)
class EmpControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private EmpService empService;

    @MockBean
    private AccessLogService accessLogService;

    @Test
    void createEmpReturnsCreatedEmp() throws Exception {
        OffsetDateTime now = OffsetDateTime.parse("2026-04-04T00:00:00Z");
        CreateEmpRequest request = new CreateEmpRequest(
                2L,
                1L,
                100L,
                10L,
                null,
                null,
                "EMP-0001",
                "Kim Finance",
                "TEAM_LEADER",
                "Finance Manager",
                LocalDate.parse("2026-01-01"),
                null,
                "ACTIVE"
        );
        EmpResponse response = new EmpResponse(
                501L,
                2L,
                1L,
                100L,
                10L,
                null,
                null,
                "EMP-0001",
                "Kim Finance",
                "TEAM_LEADER",
                "Finance Manager",
                LocalDate.parse("2026-01-01"),
                null,
                "ACTIVE",
                now,
                now
        );

        given(empService.createEmp(request)).willReturn(response);

        mockMvc.perform(post("/api/v1/employees")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.empId").value(501))
                .andExpect(jsonPath("$.empNo").value("EMP-0001"))
                .andExpect(jsonPath("$.teamRoleCd").value("TEAM_LEADER"));
    }

    @Test
    void getEmpsReturnsList() throws Exception {
        OffsetDateTime now = OffsetDateTime.parse("2026-04-04T00:00:00Z");
        EmpListResponse response = new EmpListResponse(
                1,
                List.of(new EmpResponse(
                        501L,
                        2L,
                        1L,
                        100L,
                        10L,
                        null,
                        null,
                        "EMP-0001",
                        "Kim Finance",
                        "TEAM_LEADER",
                        "Finance Manager",
                        LocalDate.parse("2026-01-01"),
                        null,
                        "ACTIVE",
                        now,
                        now
                ))
        );

        given(empService.getEmps(2L, 1L, 10L)).willReturn(response);

        mockMvc.perform(get("/api/v1/employees")
                        .param("tenantId", "2")
                        .param("coId", "1")
                        .param("deptId", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totCnt").value(1))
                .andExpect(jsonPath("$.itemList[0].empNm").value("Kim Finance"));
    }
}
