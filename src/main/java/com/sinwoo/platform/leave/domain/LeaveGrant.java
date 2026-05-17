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
 * 직원별 연도별 휴가 부여 내역.
 * 매년 자동 또는 customer admin 수동으로 row 생성.
 * USED_DAYS / EXPIRED_DAYS는 캐시 — TB_LEAVE_REQ로부터 재계산 가능.
 */
@Getter
@Entity
@Table(
        name = "TB_LEAVE_GRANT",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "UK_TB_LEAVE_GRANT_TENANT_CO_EMP_YR",
                        columnNames = {"TENANT_ID", "CO_ID", "EMP_ID", "GRANT_YR"}
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LeaveGrant extends BaseEntity {

    @Column(name = "TENANT_ID", nullable = false, length = 20)
    private String tenantId;

    @Column(name = "CO_ID", nullable = false, length = 20)
    private String coId;

    @Column(name = "EMP_ID", nullable = false, length = 20)
    private String empId;

    @Column(name = "GRANT_YR", nullable = false)
    private Short grantYr;

    /** Days granted at start of year */
    @Column(name = "GRANT_DAYS", nullable = false, precision = 4, scale = 1)
    private BigDecimal grantDays;

    /** Days carried over from previous year (after expiry handling) */
    @Column(name = "CARRYOVER_DAYS", nullable = false, precision = 4, scale = 1)
    private BigDecimal carryoverDays;

    /** Cache: days used this year (from TB_LEAVE_REQ) */
    @Column(name = "USED_DAYS", nullable = false, precision = 4, scale = 1)
    private BigDecimal usedDays;

    /** Cache: carryover days expired without being used */
    @Column(name = "EXPIRED_DAYS", nullable = false, precision = 4, scale = 1)
    private BigDecimal expiredDays;

    @Column(name = "REMARK", length = 500)
    private String remark;

    @Column(name = "DEL_YN", nullable = false, columnDefinition = "CHAR(1)")
    private String delYn;

    private LeaveGrant(
            String tenantId, String coId, String empId, Short grantYr,
            BigDecimal grantDays, BigDecimal carryoverDays,
            BigDecimal usedDays, BigDecimal expiredDays,
            String remark
    ) {
        this.tenantId = tenantId;
        this.coId = coId;
        this.empId = empId;
        this.grantYr = grantYr;
        this.grantDays = grantDays;
        this.carryoverDays = carryoverDays;
        this.usedDays = usedDays;
        this.expiredDays = expiredDays;
        this.remark = remark;
        this.delYn = "N";
    }

    public static LeaveGrant create(
            String tenantId, String coId, String empId, Short grantYr,
            BigDecimal grantDays, BigDecimal carryoverDays,
            String remark
    ) {
        return new LeaveGrant(
                tenantId, coId, empId, grantYr,
                grantDays, carryoverDays,
                BigDecimal.ZERO, BigDecimal.ZERO,
                remark
        );
    }

    public void update(
            BigDecimal grantDays, BigDecimal carryoverDays,
            String remark
    ) {
        this.grantDays = grantDays;
        this.carryoverDays = carryoverDays;
        this.remark = remark;
    }

    public void refreshUsedDays(BigDecimal usedDays) {
        this.usedDays = usedDays != null ? usedDays : BigDecimal.ZERO;
    }

    public void markExpired(BigDecimal expiredDays) {
        this.expiredDays = expiredDays != null ? expiredDays : BigDecimal.ZERO;
    }

    public void softDelete() {
        this.delYn = "Y";
    }

    public boolean isDeleted() {
        return "Y".equals(this.delYn);
    }

    /** 사용 가능 일수 = (이월 - 만료) + 부여 - 사용 */
    public BigDecimal getAvailableDays() {
        BigDecimal carryoverActive = carryoverDays.subtract(expiredDays);
        if (carryoverActive.signum() < 0) carryoverActive = BigDecimal.ZERO;
        BigDecimal total = carryoverActive.add(grantDays).subtract(usedDays);
        return total.signum() < 0 ? BigDecimal.ZERO : total;
    }

    /** 이월 잔여 (만료 차감 후) */
    public BigDecimal getCarryoverRemain() {
        BigDecimal remain = carryoverDays.subtract(expiredDays);
        return remain.signum() < 0 ? BigDecimal.ZERO : remain;
    }
}
