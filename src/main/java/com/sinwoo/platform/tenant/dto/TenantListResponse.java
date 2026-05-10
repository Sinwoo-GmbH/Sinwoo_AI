package com.sinwoo.platform.tenant.dto;

import java.util.List;

public record TenantListResponse(
        long totCnt,
        List<TenantResponse> itemList
) {
}
