package com.sinwoo.platform.user.controller;

import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sinwoo.common.audit.accesslog.AccessLogService;
import com.sinwoo.common.security.SecurityConfig;
import com.sinwoo.platform.user.dto.CreateUsrRequest;
import com.sinwoo.platform.user.dto.UsrListResponse;
import com.sinwoo.platform.user.dto.UsrResponse;
import com.sinwoo.platform.user.service.UsrService;
import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = UsrController.class)
@Import(SecurityConfig.class)
class UsrControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UsrService usrService;

    @MockBean
    private AccessLogService accessLogService;

    @Test
    void createUsrReturnsCreatedUsr() throws Exception {
        OffsetDateTime now = OffsetDateTime.parse("2026-04-04T00:00:00Z");
        CreateUsrRequest request = new CreateUsrRequest(
                2L,
                1L,
                "master.admin",
                "master.admin@sinwoo.local",
                "Sinwoo123!",
                "Master Admin",
                "ko",
                "010-0000-0000",
                "ADMIN",
                "9",
                "ACTIVE",
                List.of("ROLE_ADMIN", "ROLE_FINANCE")
        );
        UsrResponse response = new UsrResponse(
                1L,
                2L,
                1L,
                "MASTER.ADMIN",
                "master.admin@sinwoo.local",
                "Master Admin",
                "ko",
                "010-0000-0000",
                "ADMIN",
                "9",
                "ACTIVE",
                List.of("ROLE_ADMIN", "ROLE_FINANCE"),
                now,
                now
        );

        given(usrService.createUsr(request)).willReturn(response);

        mockMvc.perform(post("/api/v1/users")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.usrId").value(1))
                .andExpect(jsonPath("$.tenantId").value(2))
                .andExpect(jsonPath("$.coId").value(1))
                .andExpect(jsonPath("$.lgnId").value("MASTER.ADMIN"))
                .andExpect(jsonPath("$.authGrpCd").value("ADMIN"))
                .andExpect(jsonPath("$.authLvlCd").value("9"))
                .andExpect(jsonPath("$.roleCds[0]").value("ROLE_ADMIN"))
                .andExpect(jsonPath("$.roleCds[1]").value("ROLE_FINANCE"));
    }

    @Test
    void getUsrsReturnsAdminListResponse() throws Exception {
        OffsetDateTime now = OffsetDateTime.parse("2026-04-04T00:00:00Z");
        UsrListResponse response = new UsrListResponse(
                1,
                List.of(new UsrResponse(
                        1L,
                        2L,
                        1L,
                        "MASTER.ADMIN",
                        "master.admin@sinwoo.local",
                        "Master Admin",
                        "ko",
                        "010-0000-0000",
                        "ADMIN",
                        "9",
                        "ACTIVE",
                        List.of("ROLE_ADMIN", "ROLE_FINANCE"),
                        now,
                        now
                ))
        );

        given(usrService.getUsrs(2L, 1L)).willReturn(response);

        mockMvc.perform(get("/api/v1/users")
                        .param("tenantId", "2")
                        .param("coId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totCnt").value(1))
                .andExpect(jsonPath("$.itemList[0].lgnId").value("MASTER.ADMIN"))
                .andExpect(jsonPath("$.itemList[0].authGrpCd").value("ADMIN"))
                .andExpect(jsonPath("$.itemList[0].roleCds[0]").value("ROLE_ADMIN"));
    }
}
