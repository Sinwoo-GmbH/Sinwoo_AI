package com.sinwoo.billing.dto;

import java.util.List;

public record SubscriptionListResponse(
        long totCnt,
        List<SubscriptionResponse> itemList
) {
}
