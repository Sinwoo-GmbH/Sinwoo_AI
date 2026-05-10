package com.sinwoo.platform.billing.dto;

import java.util.List;

public record PayTxnListResponse(
        long totCnt,
        List<PayTxnResponse> itemList
) {
}
