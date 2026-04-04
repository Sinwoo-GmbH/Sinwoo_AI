package com.sinwoo.billing.domain;

import com.sinwoo.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "TB_PAY_TXN")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PaymentTransaction extends BaseEntity {

    @Column(name = "TENANT_ID", nullable = false)
    private Long tenantId;

    @Column(name = "SUBS_ID", nullable = false)
    private Long subsId;

    @Column(name = "PAY_TP_CD", nullable = false, length = 20)
    private String payTpCd;

    @Column(name = "PAY_STS_CD", nullable = false, length = 20)
    private String payStsCd;

    @Column(name = "PAY_AMT", nullable = false, precision = 15, scale = 2)
    private BigDecimal payAmt;

    @Column(name = "CURR_CD", nullable = false, length = 10)
    private String currCd;

    @Column(name = "PG_CD", length = 30)
    private String pgCd;

    @Column(name = "PG_TXN_NO", length = 100)
    private String pgTxnNo;

    @Column(name = "APRV_DTM")
    private OffsetDateTime aprvDtm;

    @Column(name = "FAIL_MSG", length = 1000)
    private String failMsg;

    private PaymentTransaction(
            Long tenantId,
            Long subsId,
            String payTpCd,
            String payStsCd,
            BigDecimal payAmt,
            String currCd,
            String pgCd,
            String pgTxnNo,
            OffsetDateTime aprvDtm,
            String failMsg
    ) {
        this.tenantId = tenantId;
        this.subsId = subsId;
        this.payTpCd = payTpCd;
        this.payStsCd = payStsCd;
        this.payAmt = payAmt;
        this.currCd = currCd;
        this.pgCd = pgCd;
        this.pgTxnNo = pgTxnNo;
        this.aprvDtm = aprvDtm;
        this.failMsg = failMsg;
    }

    public static PaymentTransaction create(
            Long tenantId,
            Long subsId,
            String payTpCd,
            String payStsCd,
            BigDecimal payAmt,
            String currCd,
            String pgCd,
            String pgTxnNo,
            OffsetDateTime aprvDtm,
            String failMsg
    ) {
        return new PaymentTransaction(tenantId, subsId, payTpCd, payStsCd, payAmt, currCd, pgCd, pgTxnNo, aprvDtm, failMsg);
    }
}
