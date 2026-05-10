package com.sinwoo.platform.billing.controller;

import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sinwoo.platform.billing.dto.CreateSubscrPlanRequest;
import com.sinwoo.platform.billing.dto.SubscrPlanListResponse;
import com.sinwoo.platform.billing.dto.SubscrPlanResponse;
import com.sinwoo.platform.billing.service.SubscrPlanService;
import com.sinwoo.common.audit.accesslog.AccessLogService;
import com.sinwoo.common.security.SecurityConfig;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = SubscrPlanController.class)
@Import(SecurityConfig.class)
class SubscrPlanControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private SubscrPlanService subscrPlanService;

    @MockBean
    private AccessLogService accessLogService;

    @Test
    void createSubscrPlanReturnsCreatedSubscrPlan() throws Exception {
        OffsetDateTime now = OffsetDateTime.parse("2026-04-04T00:00:00Z");
        CreateSubscrPlanRequest request = new CreateSubscrPlanRequest("PLAN_B2B_BASIC", "B2B Basic Plan", "CUSTOMER", "MONTHLY", "EUR", new BigDecimal("99.00"), 30, "Y");
        SubscrPlanResponse response = new SubscrPlanResponse(2L, "PLAN_B2B_BASIC", "B2B Basic Plan", "CUSTOMER", "MONTHLY", "EUR", new BigDecimal("99.00"), 30, "Y", now, now);

        given(subscrPlanService.createSubscrPlan(request)).willReturn(response);

        mockMvc.perform(post("/api/v1/subscription-plans")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.planCd").value("PLAN_B2B_BASIC"))
                .andExpect(jsonPath("$.tenantTpCd").value("CUSTOMER"))
                .andExpect(jsonPath("$.baseAmt").value(99.00));
    }

    @Test
    void getSubscrPlansReturnsList() throws Exception {
        OffsetDateTime now = OffsetDateTime.parse("2026-04-04T00:00:00Z");
        SubscrPlanListResponse response = new SubscrPlanListResponse(
                2,
                List.of(
                        new SubscrPlanResponse(1L, "PLAN_INTERNAL_FREE", "Internal Free Plan", "INTERNAL", "MONTHLY", "EUR", BigDecimal.ZERO, null, "Y", now, now),
                        new SubscrPlanResponse(2L, "PLAN_B2B_BASIC", "B2B Basic Plan", "CUSTOMER", "MONTHLY", "EUR", new BigDecimal("99.00"), 30, "Y", now, now)
                )
        );

        given(subscrPlanService.getSubscrPlans()).willReturn(response);

        mockMvc.perform(get("/api/v1/subscription-plans"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totCnt").value(2))
                .andExpect(jsonPath("$.itemList[0].planCd").value("PLAN_INTERNAL_FREE"))
                .andExpect(jsonPath("$.itemList[1].planCd").value("PLAN_B2B_BASIC"));
    }
}
