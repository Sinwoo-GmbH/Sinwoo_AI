package com.sinwoo.billing.controller;

import com.sinwoo.billing.dto.CreateSubscriptionPlanRequest;
import com.sinwoo.billing.dto.SubscriptionPlanListResponse;
import com.sinwoo.billing.dto.SubscriptionPlanResponse;
import com.sinwoo.billing.service.SubscriptionPlanService;
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
public class SubscriptionPlanController {

    private final SubscriptionPlanService subscriptionPlanService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SubscriptionPlanResponse createSubscriptionPlan(@Valid @RequestBody CreateSubscriptionPlanRequest request) {
        return subscriptionPlanService.createSubscriptionPlan(request);
    }

    @GetMapping
    public SubscriptionPlanListResponse getSubscriptionPlans() {
        return subscriptionPlanService.getSubscriptionPlans();
    }
}
