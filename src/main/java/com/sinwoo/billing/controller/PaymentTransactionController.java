package com.sinwoo.billing.controller;

import com.sinwoo.billing.dto.CreatePaymentTransactionRequest;
import com.sinwoo.billing.dto.PaymentTransactionListResponse;
import com.sinwoo.billing.dto.PaymentTransactionResponse;
import com.sinwoo.billing.service.PaymentTransactionService;
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
public class PaymentTransactionController {

    private final PaymentTransactionService paymentTransactionService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PaymentTransactionResponse createPaymentTransaction(@Valid @RequestBody CreatePaymentTransactionRequest request) {
        return paymentTransactionService.createPaymentTransaction(request);
    }

    @GetMapping
    public PaymentTransactionListResponse getPaymentTransactions(@RequestParam Long tenantId) {
        return paymentTransactionService.getPaymentTransactions(tenantId);
    }
}
