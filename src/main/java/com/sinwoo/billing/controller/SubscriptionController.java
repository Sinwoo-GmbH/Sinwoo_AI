package com.sinwoo.billing.controller;

import com.sinwoo.billing.dto.CreateSubscriptionRequest;
import com.sinwoo.billing.dto.SubscriptionListResponse;
import com.sinwoo.billing.dto.SubscriptionResponse;
import com.sinwoo.billing.service.SubscriptionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/subscriptions")
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SubscriptionResponse createSubscription(@Valid @RequestBody CreateSubscriptionRequest request) {
        return subscriptionService.createSubscription(request);
    }

    @GetMapping
    public SubscriptionListResponse getSubscriptions(@RequestParam Long tenantId) {
        return subscriptionService.getSubscriptions(tenantId);
    }
}
