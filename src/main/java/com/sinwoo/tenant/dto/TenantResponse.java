package com.sinwoo.tenant.dto;

import com.sinwoo.tenant.domain.Tenant;
import java.time.OffsetDateTime;

public record TenantResponse(
        Long tenantId,
        String tenantCd,
        String tenantNm,
        String stsCd,
        OffsetDateTime crtDtm,
        OffsetDateTime updDtm
) {
    public static TenantResponse from(Tenant tenant) {
        return new TenantResponse(
                tenant.getId(),
                tenant.getTenantCd(),
                tenant.getTenantNm(),
                tenant.getStsCd(),
                tenant.getCreatedAt(),
                tenant.getUpdatedAt()
        );
    }
}
