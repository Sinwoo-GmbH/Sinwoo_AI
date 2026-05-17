package com.sinwoo.platform.wrktm.domain;

import com.sinwoo.common.util.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDate;
import java.time.LocalTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
        name = "TB_WORK_TIME",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "UK_TB_WORK_TIME_TENANT_CO_EMP_DT",
                        columnNames = {"TENANT_ID", "CO_ID", "EMP_ID", "WORK_DT"}
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WrkTm extends BaseEntity {

    @Column(name = "TENANT_ID", nullable = false)
    private Long tenantId;

    @Column(name = "CO_ID", nullable = false)
    private Long coId;

    @Column(name = "EMP_ID", nullable = false)
    private Long empId;

    @Column(name = "WORK_DT", nullable = false)
    private LocalDate workDt;

    @Column(name = "STR_TM")
    private LocalTime strTm;

    @Column(name = "END_TM")
    private LocalTime endTm;

    @Column(name = "WORK_MIN")
    private Short workMin;

    @Column(name = "RMK", length = 500)
    private String rmk;

    @Column(name = "DEL_YN", nullable = false, columnDefinition = "CHAR(1)")
    private String delYn;

    private WrkTm(
            Long tenantId,
            Long coId,
            Long empId,
            LocalDate workDt,
            LocalTime strTm,
            LocalTime endTm,
            Short workMin,
            String rmk
    ) {
        this.tenantId = tenantId;
        this.coId = coId;
        this.empId = empId;
        this.workDt = workDt;
        this.strTm = strTm;
        this.endTm = endTm;
        this.workMin = workMin;
        this.rmk = rmk;
        this.delYn = "N";
    }

    public static WrkTm create(
            Long tenantId,
            Long coId,
            Long empId,
            LocalDate workDt,
            LocalTime strTm,
            LocalTime endTm,
            Short workMin,
            String rmk
    ) {
        return new WrkTm(tenantId, coId, empId, workDt, strTm, endTm, workMin, rmk);
    }

    public void updateTimes(LocalTime strTm, LocalTime endTm, Short workMin, String rmk) {
        this.strTm = strTm;
        this.endTm = endTm;
        this.workMin = workMin;
        this.rmk = rmk;
    }

    public void clockIn(LocalTime strTm) {
        this.strTm = strTm;
    }

    public void clockOut(LocalTime endTm, Short workMin) {
        this.endTm = endTm;
        this.workMin = workMin;
    }

    public void softDelete() {
        this.delYn = "Y";
    }

    public boolean isDeleted() {
        return "Y".equals(this.delYn);
    }
}
