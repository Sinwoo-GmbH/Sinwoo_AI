package com.sinwoo.platform.billing.dto;

import com.sinwoo.platform.billing.domain.Subscr;
import java.time.LocalDate;
import java.time.OffsetDateTime;

public record SubscrResponse(
        Long subsId,
        Long tenantId,
        Long planId,
        String planCd,
        String planNm,
        String subsStsCd,
        String billFreeYn,
        String autoPayYn,
        LocalDate strDt,
        LocalDate endDt,
        LocalDate nextBillDt,
        OffsetDateTime crtDtm,
        OffsetDateTime updDtm
) {
    public static SubscrResponse from(Subscr subscr, String planCd, String planNm) {
        return new SubscrResponse(
                subscr.getId(),
                subscr.getTenantId(),
                subscr.getPlanId(),
                planCd,
                planNm,
                subscr.getSubsStsCd(),
                subscr.getBillFreeYn(),
                subscr.getAutoPayYn(),
                subscr.getStrDt(),
                subscr.getEndDt(),
                subscr.getNextBillDt(),
                subscr.getCreatedAt(),
                subscr.getUpdatedAt()
        );
    }
}
