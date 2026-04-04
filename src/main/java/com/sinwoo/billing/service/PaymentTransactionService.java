package com.sinwoo.billing.service;

import com.sinwoo.billing.dto.CreatePaymentTransactionRequest;
import com.sinwoo.billing.dto.PaymentTransactionListResponse;
import com.sinwoo.billing.dto.PaymentTransactionResponse;

public interface PaymentTransactionService {

    PaymentTransactionResponse createPaymentTransaction(CreatePaymentTransactionRequest request);

    PaymentTransactionListResponse getPaymentTransactions(Long tenantId);
}
