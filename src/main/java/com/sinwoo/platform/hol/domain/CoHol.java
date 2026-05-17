package com.sinwoo.platform.hol.domain;

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
@Table(name = "TB_CO_HOLIDAY")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CoHol extends BaseEntity {

    @Column(name = "TENANT_ID", nullable = false)
    private Long tenantId;

    @Column(name = "CO_ID", nullable = false)
    private Long coId;

    @Column(name = "HOLIDAY_NM", nullable = false, length = 255)
    private String holidayNm;

    @Column(name = "STR_DT", nullable = false)
    private LocalDate strDt;

    @Column(name = "END_DT", nullable = false)
    private LocalDate endDt;

    @Column(name = "ANNUAL_YN", nullable = false, columnDefinition = "CHAR(1)")
    private String annualYn;

    @Column(name = "APPLY_YR", nullable = false)
    private Short applyYr;

    @Column(name = "DEL_YN", nullable = false, columnDefinition = "CHAR(1)")
    private String delYn;

    private CoHol(
            Long tenantId,
            Long coId,
            String holidayNm,
            LocalDate strDt,
            LocalDate endDt,
            String annualYn,
            Short applyYr
    ) {
        this.tenantId = tenantId;
        this.coId = coId;
        this.holidayNm = holidayNm;
        this.strDt = strDt;
        this.endDt = endDt;
        this.annualYn = annualYn;
        this.applyYr = applyYr;
        this.delYn = "N";
    }

    public static CoHol create(
            Long tenantId,
            Long coId,
            String holidayNm,
            LocalDate strDt,
            LocalDate endDt,
            String annualYn,
            Short applyYr
    ) {
        return new CoHol(tenantId, coId, holidayNm, strDt, endDt, annualYn, applyYr);
    }

    public void update(
            String holidayNm,
            LocalDate strDt,
            LocalDate endDt,
            String annualYn,
            Short applyYr
    ) {
        this.holidayNm = holidayNm;
        this.strDt = strDt;
        this.endDt = endDt;
        this.annualYn = annualYn;
        this.applyYr = applyYr;
    }

    public void softDelete() {
        this.delYn = "Y";
    }

    public boolean isDeleted() {
        return "Y".equals(this.delYn);
    }
}
