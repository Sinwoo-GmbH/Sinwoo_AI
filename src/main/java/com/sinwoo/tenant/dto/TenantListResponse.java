package com.sinwoo.tenant.dto;

import java.util.List;

public record TenantListResponse(
        long count,
        List<TenantResponse> items
) {
}
