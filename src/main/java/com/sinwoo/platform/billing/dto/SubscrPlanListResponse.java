package com.sinwoo.platform.billing.dto;

import java.util.List;

public record SubscrPlanListResponse(
        long totCnt,
        List<SubscrPlanResponse> itemList
) {
}
