package com.sinwoo.platform.billing.controller;

import com.sinwoo.platform.billing.dto.CreateSubscrPlanRequest;
import com.sinwoo.platform.billing.dto.SubscrPlanListResponse;
import com.sinwoo.platform.billing.dto.SubscrPlanResponse;
import com.sinwoo.platform.billing.service.SubscrPlanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/subscription-plans")
@RequiredArgsConstructor
public class SubscrPlanController {

    private final SubscrPlanService subscrPlanService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SubscrPlanResponse createSubscrPlan(@Valid @RequestBody CreateSubscrPlanRequest request) {
        return subscrPlanService.createSubscrPlan(request);
    }

    @GetMapping
    public SubscrPlanListResponse getSubscrPlans() {
        return subscrPlanService.getSubscrPlans();
    }
}
