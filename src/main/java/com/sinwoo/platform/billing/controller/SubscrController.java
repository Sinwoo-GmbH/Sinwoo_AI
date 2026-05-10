package com.sinwoo.platform.billing.controller;

import com.sinwoo.platform.billing.dto.CreateSubscrRequest;
import com.sinwoo.platform.billing.dto.SubscrListResponse;
import com.sinwoo.platform.billing.dto.SubscrResponse;
import com.sinwoo.platform.billing.service.SubscrService;
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
public class SubscrController {

    private final SubscrService subscrService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SubscrResponse createSubscr(@Valid @RequestBody CreateSubscrRequest request) {
        return subscrService.createSubscr(request);
    }

    @GetMapping
    public SubscrListResponse getSubscrs(@RequestParam Long tenantId) {
        return subscrService.getSubscrs(tenantId);
    }
}
