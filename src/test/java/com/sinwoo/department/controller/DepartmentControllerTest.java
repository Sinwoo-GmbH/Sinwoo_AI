package com.sinwoo.department.controller;

import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sinwoo.common.accesslog.AccessLogService;
import com.sinwoo.common.security.SecurityConfig;
import com.sinwoo.department.dto.CreateDepartmentRequest;
import com.sinwoo.department.dto.DepartmentListResponse;
import com.sinwoo.department.dto.DepartmentNodeResponse;
import com.sinwoo.department.dto.DepartmentResponse;
import com.sinwoo.department.dto.DepartmentTreeResponse;
import com.sinwoo.department.service.DepartmentService;
import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = DepartmentController.class)
@Import(SecurityConfig.class)
class DepartmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private DepartmentService departmentService;

    @MockBean
    private AccessLogService accessLogService;

    @Test
    void createDepartmentReturnsCreatedDepartment() throws Exception {
        OffsetDateTime now = OffsetDateTime.parse("2026-04-04T00:00:00Z");
        CreateDepartmentRequest request = new CreateDepartmentRequest(2L, 1L, "FIN", "Finance Team", null, "ACTIVE");
        DepartmentResponse response = new DepartmentResponse(10L, 2L, 1L, "FIN", "Finance Team", null, 1, "ACTIVE", now, now);

        given(departmentService.createDepartment(request)).willReturn(response);

        mockMvc.perform(post("/api/v1/departments")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.deptId").value(10))
                .andExpect(jsonPath("$.deptCd").value("FIN"))
                .andExpect(jsonPath("$.deptLvlNo").value(1));
    }

    @Test
    void getDepartmentsReturnsFlatList() throws Exception {
        OffsetDateTime now = OffsetDateTime.parse("2026-04-04T00:00:00Z");
        DepartmentListResponse response = new DepartmentListResponse(
                1,
                List.of(new DepartmentResponse(10L, 2L, 1L, "FIN", "Finance Team", null, 1, "ACTIVE", now, now))
        );

        given(departmentService.getDepartments(2L, 1L)).willReturn(response);

        mockMvc.perform(get("/api/v1/departments")
                        .param("tenantId", "2")
                        .param("coId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totCnt").value(1))
                .andExpect(jsonPath("$.itemList[0].deptNm").value("Finance Team"));
    }

    @Test
    void getDepartmentTreeReturnsHierarchy() throws Exception {
        DepartmentTreeResponse response = new DepartmentTreeResponse(
                1,
                List.of(new DepartmentNodeResponse(10L, 2L, 1L, "HQ", "Headquarters", null, 1, "ACTIVE", List.of(
                        new DepartmentNodeResponse(11L, 2L, 1L, "FIN", "Finance Team", 10L, 2, "ACTIVE", List.of())
                )))
        );

        given(departmentService.getDepartmentTree(2L, 1L)).willReturn(response);

        mockMvc.perform(get("/api/v1/departments/tree")
                        .param("tenantId", "2")
                        .param("coId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totCnt").value(1))
                .andExpect(jsonPath("$.itemList[0].deptCd").value("HQ"))
                .andExpect(jsonPath("$.itemList[0].childList[0].deptCd").value("FIN"));
    }
}
