package com.sinwoo.platform.billing.domain;

import com.sinwoo.common.util.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "TB_SUBS")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Subscr extends BaseEntity {

    @Column(name = "TENANT_ID", nullable = false)
    private Long tenantId;

    @Column(name = "PLAN_ID", nullable = false)
    private Long planId;

    @Column(name = "SUBS_STS_CD", nullable = false, length = 20)
    private String subsStsCd;

    @Column(name = "BILL_FREE_YN", nullable = false, length = 1, columnDefinition = "CHAR(1)")
    private String billFreeYn;

    @Column(name = "AUTO_PAY_YN", nullable = false, length = 1, columnDefinition = "CHAR(1)")
    private String autoPayYn;

    @Column(name = "STR_DT", nullable = false)
    private LocalDate strDt;

    @Column(name = "END_DT")
    private LocalDate endDt;

    @Column(name = "NEXT_BILL_DT")
    private LocalDate nextBillDt;

    private Subscr(
            Long tenantId,
            Long planId,
            String subsStsCd,
            String billFreeYn,
            String autoPayYn,
            LocalDate strDt,
            LocalDate endDt,
            LocalDate nextBillDt
    ) {
        this.tenantId = tenantId;
        this.planId = planId;
        this.subsStsCd = subsStsCd;
        this.billFreeYn = billFreeYn;
        this.autoPayYn = autoPayYn;
        this.strDt = strDt;
        this.endDt = endDt;
        this.nextBillDt = nextBillDt;
    }

    public static Subscr create(
            Long tenantId,
            Long planId,
            String subsStsCd,
            String billFreeYn,
            String autoPayYn,
            LocalDate strDt,
            LocalDate endDt,
            LocalDate nextBillDt
    ) {
        return new Subscr(tenantId, planId, subsStsCd, billFreeYn, autoPayYn, strDt, endDt, nextBillDt);
    }
}
