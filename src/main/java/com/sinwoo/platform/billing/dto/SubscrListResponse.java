package com.sinwoo.platform.billing.dto;

import java.util.List;

public record SubscrListResponse(
        long totCnt,
        List<SubscrResponse> itemList
) {
}
