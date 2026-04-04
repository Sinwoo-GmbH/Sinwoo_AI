package com.sinwoo.billing.domain;

import com.sinwoo.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
        name = "TB_SUBS_PLAN",
        uniqueConstraints = {
                @UniqueConstraint(name = "UK_TB_SUBS_PLAN_PLAN_CD", columnNames = "PLAN_CD")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SubscriptionPlan extends BaseEntity {

    @Column(name = "PLAN_CD", nullable = false, length = 100)
    private String planCd;

    @Column(name = "PLAN_NM", nullable = false, length = 255)
    private String planNm;

    @Column(name = "TENANT_TP_CD", nullable = false, length = 20)
    private String tenantTpCd;

    @Column(name = "BILL_CYCL_CD", nullable = false, length = 20)
    private String billCyclCd;

    @Column(name = "CURR_CD", nullable = false, length = 10)
    private String currCd;

    @Column(name = "BASE_AMT", nullable = false, precision = 15, scale = 2)
    private BigDecimal baseAmt;

    @Column(name = "USR_LMT_CNT")
    private Integer usrLmtCnt;

    @Column(name = "USE_YN", nullable = false, length = 1, columnDefinition = "CHAR(1)")
    private String useYn;

    private SubscriptionPlan(
            String planCd,
            String planNm,
            String tenantTpCd,
            String billCyclCd,
            String currCd,
            BigDecimal baseAmt,
            Integer usrLmtCnt,
            String useYn
    ) {
        this.planCd = planCd;
        this.planNm = planNm;
        this.tenantTpCd = tenantTpCd;
        this.billCyclCd = billCyclCd;
        this.currCd = currCd;
        this.baseAmt = baseAmt;
        this.usrLmtCnt = usrLmtCnt;
        this.useYn = useYn;
    }

    public static SubscriptionPlan create(
            String planCd,
            String planNm,
            String tenantTpCd,
            String billCyclCd,
            String currCd,
            BigDecimal baseAmt,
            Integer usrLmtCnt,
            String useYn
    ) {
        return new SubscriptionPlan(planCd, planNm, tenantTpCd, billCyclCd, currCd, baseAmt, usrLmtCnt, useYn);
    }
}
