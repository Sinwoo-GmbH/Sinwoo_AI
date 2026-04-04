package com.sinwoo.billing.dto;

import com.sinwoo.billing.domain.Subscription;
import java.time.LocalDate;
import java.time.OffsetDateTime;

public record SubscriptionResponse(
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
    public static SubscriptionResponse from(Subscription subscription, String planCd, String planNm) {
        return new SubscriptionResponse(
                subscription.getId(),
                subscription.getTenantId(),
                subscription.getPlanId(),
                planCd,
                planNm,
                subscription.getSubsStsCd(),
                subscription.getBillFreeYn(),
                subscription.getAutoPayYn(),
                subscription.getStrDt(),
                subscription.getEndDt(),
                subscription.getNextBillDt(),
                subscription.getCreatedAt(),
                subscription.getUpdatedAt()
        );
    }
}
