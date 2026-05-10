package com.sinwoo.platform.billing.controller;

import com.sinwoo.platform.billing.dto.CreatePayTxnRequest;
import com.sinwoo.platform.billing.dto.PayTxnListResponse;
import com.sinwoo.platform.billing.dto.PayTxnResponse;
import com.sinwoo.platform.billing.service.PayTxnService;
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
@RequestMapping("/api/v1/payment-transactions")
@RequiredArgsConstructor
public class PayTxnController {

    private final PayTxnService payTxnService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PayTxnResponse createPayTxn(@Valid @RequestBody CreatePayTxnRequest request) {
        return payTxnService.createPayTxn(request);
    }

    @GetMapping
    public PayTxnListResponse getPayTxns(@RequestParam Long tenantId) {
        return payTxnService.getPayTxns(tenantId);
    }
}
