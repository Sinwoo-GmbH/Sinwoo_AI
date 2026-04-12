package com.sinwoo.employee.controller;

import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sinwoo.common.accesslog.AccessLogService;
import com.sinwoo.common.security.SecurityConfig;
import com.sinwoo.employee.dto.CreateEmployeeRequest;
import com.sinwoo.employee.dto.EmployeeListResponse;
import com.sinwoo.employee.dto.EmployeeResponse;
import com.sinwoo.employee.service.EmployeeService;
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

@WebMvcTest(controllers = EmployeeController.class)
@Import(SecurityConfig.class)
class EmployeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private EmployeeService employeeService;

    @MockBean
    private AccessLogService accessLogService;

    @Test
    void createEmployeeReturnsCreatedEmployee() throws Exception {
        OffsetDateTime now = OffsetDateTime.parse("2026-04-04T00:00:00Z");
        CreateEmployeeRequest request = new CreateEmployeeRequest(
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
        EmployeeResponse response = new EmployeeResponse(
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

        given(employeeService.createEmployee(request)).willReturn(response);

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
    void getEmployeesReturnsList() throws Exception {
        OffsetDateTime now = OffsetDateTime.parse("2026-04-04T00:00:00Z");
        EmployeeListResponse response = new EmployeeListResponse(
                1,
                List.of(new EmployeeResponse(
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

        given(employeeService.getEmployees(2L, 1L, 10L)).willReturn(response);

        mockMvc.perform(get("/api/v1/employees")
                        .param("tenantId", "2")
                        .param("coId", "1")
                        .param("deptId", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totCnt").value(1))
                .andExpect(jsonPath("$.itemList[0].empNm").value("Kim Finance"));
    }
}
