package com.sinwoo.platform.billing.dto;

import com.sinwoo.platform.billing.domain.PayTxn;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record PayTxnResponse(
        Long payTxnId,
        Long tenantId,
        Long subsId,
        String payTpCd,
        String payStsCd,
        BigDecimal payAmt,
        String currCd,
        String pgCd,
        String pgTxnNo,
        OffsetDateTime aprvDtm,
        String failMsg,
        OffsetDateTime crtDtm,
        OffsetDateTime updDtm
) {
    public static PayTxnResponse from(PayTxn payTxn) {
        return new PayTxnResponse(
                payTxn.getId(),
                payTxn.getTenantId(),
                payTxn.getSubsId(),
                payTxn.getPayTpCd(),
                payTxn.getPayStsCd(),
                payTxn.getPayAmt(),
                payTxn.getCurrCd(),
                payTxn.getPgCd(),
                payTxn.getPgTxnNo(),
                payTxn.getAprvDtm(),
                payTxn.getFailMsg(),
                payTxn.getCreatedAt(),
                payTxn.getUpdatedAt()
        );
    }
}
