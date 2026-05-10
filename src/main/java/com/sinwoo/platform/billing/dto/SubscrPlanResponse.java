package com.sinwoo.platform.billing.dto;

import com.sinwoo.platform.billing.domain.SubscrPlan;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record SubscrPlanResponse(
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
    public static SubscrPlanResponse from(SubscrPlan plan) {
        return new SubscrPlanResponse(
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
