package com.sinwoo.billing.dto;

import com.sinwoo.billing.domain.SubscriptionPlan;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record SubscriptionPlanResponse(
        Long planId,
        String planCd,
        String planNm,
        String tenantTpCd,
        String billCyclCd,
        String currCd,
        BigDecimal baseAmt,
        Integer usrLmtCnt,
        String useYn,
        OffsetDateTime crtDtm,
        OffsetDateTime updDtm
) {
    public static SubscriptionPlanResponse from(SubscriptionPlan plan) {
        return new SubscriptionPlanResponse(
                plan.getId(),
                plan.getPlanCd(),
                plan.getPlanNm(),
                plan.getTenantTpCd(),
                plan.getBillCyclCd(),
                plan.getCurrCd(),
                plan.getBaseAmt(),
                plan.getUsrLmtCnt(),
                plan.getUseYn(),
                plan.getCreatedAt(),
                plan.getUpdatedAt()
        );
    }
}
