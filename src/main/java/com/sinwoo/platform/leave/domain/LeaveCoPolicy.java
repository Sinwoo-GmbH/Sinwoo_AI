package com.sinwoo.platform.leave.domain;

import com.sinwoo.common.util.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 회사별 휴가 정책 (1 row per company).
 * customer admin이 화면에서 수정.
 */
@Getter
@Entity
@Table(
        name = "TB_LEAVE_CO_POLICY",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "UK_TB_LEAVE_CO_POLICY_TENANT_CO",
                        columnNames = {"TENANT_ID", "CO_ID"}
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LeaveCoPolicy extends BaseEntity {

    @Column(name = "TENANT_ID", nullable = false, length = 20)
    private String tenantId;

    @Column(name = "CO_ID", nullable = false, length = 20)
    private String coId;

    /** FIXED_YEARLY | ANNIVERSARY | FISCAL_YEAR */
    @Column(name = "GRANT_TIMING_CD", nullable = false, length = 20)
    private String grantTimingCd;

    /** Fiscal year start month (1-12). Used only when grantTimingCd=FISCAL_YEAR */
    @Column(name = "FISCAL_START_MM", nullable = false)
    private Short fiscalStartMm;

    /** FLAT | TENURE | MANUAL */
    @Column(name = "GRANT_TYPE_CD", nullable = false, length = 20)
    private String grantTypeCd;

    @Column(name = "FLAT_DAYS", nullable = false, precision = 4, scale = 1)
    private BigDecimal flatDays;

    @Column(name = "VAC_INC_DAYS", nullable = false, precision = 4, scale = 1)
    private BigDecimal vacIncDays;

    @Column(name = "VAC_INC_MAX_DAYS", precision = 4, scale = 1)
    private BigDecimal vacIncMaxDays;

    @Column(name = "CARRYOVER_ENABLED_YN", nullable = false, columnDefinition = "CHAR(1)")
    private String carryoverEnabledYn;

    /** NULL = unlimited */
    @Column(name = "CARRYOVER_MAX_DAYS", precision = 4, scale = 1)
    private BigDecimal carryoverMaxDays;

    @Column(name = "CARRYOVER_EXPIRE_MM", nullable = false)
    private Short carryoverExpireMm;

    @Column(name = "CARRYOVER_EXPIRE_DD", nullable = false)
    private Short carryoverExpireDd;

    /** AUTO_ZERO | NOTIFY_ONLY */
    @Column(name = "EXPIRE_ACTION_CD", nullable = false, length = 20)
    private String expireActionCd;

    @Column(name = "DEL_YN", nullable = false, columnDefinition = "CHAR(1)")
    private String delYn;

    private LeaveCoPolicy(
            String tenantId, String coId,
            String grantTimingCd, Short fiscalStartMm,
            String grantTypeCd, BigDecimal flatDays,
            BigDecimal vacIncDays, BigDecimal vacIncMaxDays,
            String carryoverEnabledYn, BigDecimal carryoverMaxDays,
            Short carryoverExpireMm, Short carryoverExpireDd,
            String expireActionCd
    ) {
        this.tenantId = tenantId;
        this.coId = coId;
        this.grantTimingCd = grantTimingCd;
        this.fiscalStartMm = fiscalStartMm;
        this.grantTypeCd = grantTypeCd;
        this.flatDays = flatDays;
        this.vacIncDays = vacIncDays;
        this.vacIncMaxDays = vacIncMaxDays;
        this.carryoverEnabledYn = carryoverEnabledYn;
        this.carryoverMaxDays = carryoverMaxDays;
        this.carryoverExpireMm = carryoverExpireMm;
        this.carryoverExpireDd = carryoverExpireDd;
        this.expireActionCd = expireActionCd;
        this.delYn = "N";
    }

    public static LeaveCoPolicy create(
            String tenantId, String coId,
            String grantTimingCd, Short fiscalStartMm,
            String grantTypeCd, BigDecimal flatDays,
            BigDecimal vacIncDays, BigDecimal vacIncMaxDays,
            String carryoverEnabledYn, BigDecimal carryoverMaxDays,
            Short carryoverExpireMm, Short carryoverExpireDd,
            String expireActionCd
    ) {
        return new LeaveCoPolicy(
                tenantId, coId, grantTimingCd, fiscalStartMm,
                grantTypeCd, flatDays,
                vacIncDays, vacIncMaxDays,
                carryoverEnabledYn, carryoverMaxDays,
                carryoverExpireMm, carryoverExpireDd,
                expireActionCd
        );
    }

    public void update(
            String grantTimingCd, Short fiscalStartMm,
            String grantTypeCd, BigDecimal flatDays,
            BigDecimal vacIncDays, BigDecimal vacIncMaxDays,
            String carryoverEnabledYn, BigDecimal carryoverMaxDays,
            Short carryoverExpireMm, Short carryoverExpireDd,
            String expireActionCd
    ) {
        this.grantTimingCd = grantTimingCd;
        this.fiscalStartMm = fiscalStartMm;
        this.grantTypeCd = grantTypeCd;
        this.flatDays = flatDays;
        this.vacIncDays = vacIncDays;
        this.vacIncMaxDays = vacIncMaxDays;
        this.carryoverEnabledYn = carryoverEnabledYn;
        this.carryoverMaxDays = carryoverMaxDays;
        this.carryoverExpireMm = carryoverExpireMm;
        this.carryoverExpireDd = carryoverExpireDd;
        this.expireActionCd = expireActionCd;
    }

    public boolean isCarryoverEnabled() {
        return "Y".equals(this.carryoverEnabledYn);
    }
}
