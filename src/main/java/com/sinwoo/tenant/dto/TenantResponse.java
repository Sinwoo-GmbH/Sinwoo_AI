package com.sinwoo.tenant.dto;

import com.sinwoo.tenant.domain.Tenant;
import java.time.OffsetDateTime;

public record TenantResponse(
        Long id,
        String code,
        String name,
        String status,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
    public static TenantResponse from(Tenant tenant) {
        return new TenantResponse(
                tenant.getId(),
                tenant.getCode(),
                tenant.getName(),
                tenant.getStatus(),
                tenant.getCreatedAt(),
                tenant.getUpdatedAt()
        );
    }
}
