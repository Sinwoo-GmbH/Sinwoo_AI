package com.sinwoo.platform.tenant.dto;

import com.sinwoo.platform.tenant.domain.Tenant;
import java.time.OffsetDateTime;

public record TenantResponse(
        Long tenantId,
        String tenantCd,
        String tenantNm,
        String emlDomn,
        String tenantTpCd,
        String billFreeYn,
        String stsCd,
        OffsetDateTime crtDtm,
        OffsetDateTime updDtm
) {
    public static TenantResponse from(Tenant tenant) {
        return new TenantResponse(
                tenant.getId(),
                tenant.getTenantCd(),
                tenant.getTenantNm(),
                tenant.getEmlDomn(),
                tenant.getTenantTpCd(),
                tenant.getBillFreeYn(),
                tenant.getStsCd(),
                tenant.getCreatedAt(),
                tenant.getUpdatedAt()
        );
    }
}
