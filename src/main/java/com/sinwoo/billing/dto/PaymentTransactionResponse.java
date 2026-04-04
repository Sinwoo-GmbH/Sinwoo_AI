package com.sinwoo.billing.dto;

import com.sinwoo.billing.domain.PaymentTransaction;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record PaymentTransactionResponse(
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
    public static PaymentTransactionResponse from(PaymentTransaction transaction) {
        return new PaymentTransactionResponse(
                transaction.getId(),
                transaction.getTenantId(),
                transaction.getSubsId(),
                transaction.getPayTpCd(),
                transaction.getPayStsCd(),
                transaction.getPayAmt(),
                transaction.getCurrCd(),
                transaction.getPgCd(),
                transaction.getPgTxnNo(),
                transaction.getAprvDtm(),
                transaction.getFailMsg(),
                transaction.getCreatedAt(),
                transaction.getUpdatedAt()
        );
    }
}
