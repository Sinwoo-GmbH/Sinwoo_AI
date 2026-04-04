package com.sinwoo.billing.dto;

import java.util.List;

public record SubscriptionPlanListResponse(
        long totCnt,
        List<SubscriptionPlanResponse> itemList
) {
}
