package com.sinwoo.billing.dto;

import java.util.List;

public record PaymentTransactionListResponse(
        long totCnt,
        List<PaymentTransactionResponse> itemList
) {
}
