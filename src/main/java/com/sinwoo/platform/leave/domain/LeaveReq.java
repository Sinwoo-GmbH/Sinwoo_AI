package com.sinwoo.platform.leave.domain;

import com.sinwoo.common.util.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "TB_LEAVE_REQ")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LeaveReq extends BaseEntity {

    @Column(name = "TENANT_ID", nullable = false, length = 20)
    private String tenantId;

    @Column(name = "CO_ID", nullable = false, length = 20)
    private String coId;

    @Column(name = "EMP_ID", nullable = false, length = 20)
    private String empId;

    @Column(name = "LEAVE_TP_CD", nullable = false, length = 20)
    private String leaveTpCd;

    @Column(name = "DEDUCT_TP_CD", nullable = false, length = 20)
    private String deductTpCd;

    @Column(name = "LEAVE_UNIT_CD", nullable = false, length = 20)
    private String leaveUnitCd;

    @Column(name = "STR_DT", nullable = false)
    private LocalDate strDt;

    @Column(name = "END_DT", nullable = false)
    private LocalDate endDt;

    @Column(name = "USE_DAYS", precision = 4, scale = 1)
    private BigDecimal useDays;

    @Column(name = "PRE_YR_USED", precision = 4, scale = 1)
    private BigDecimal preYrUsed;

    @Column(name = "CURR_YR_USED", precision = 4, scale = 1)
    private BigDecimal currYrUsed;

    @Column(name = "REASON", columnDefinition = "TEXT")
    private String reason;

    @Column(name = "ATCH_FILE_NM", length = 255)
    private String atchFileNm;

    @Column(name = "STS_CD", nullable = false, length = 20)
    private String stsCd;

    @Column(name = "REJ_REASON", columnDefinition = "TEXT")
    private String rejReason;

    @Column(name = "DEL_YN", nullable = false, columnDefinition = "CHAR(1)")
    private String delYn;

    private LeaveReq(
            String tenantId, String coId, String empId,
            String leaveTpCd, String deductTpCd, String leaveUnitCd,
            LocalDate strDt, LocalDate endDt,
            BigDecimal useDays, BigDecimal preYrUsed, BigDecimal currYrUsed,
            String reason, String atchFileNm, String stsCd
    ) {
        this.tenantId = tenantId;
        this.coId = coId;
        this.empId = empId;
        this.leaveTpCd = leaveTpCd;
        this.deductTpCd = deductTpCd;
        this.leaveUnitCd = leaveUnitCd;
        this.strDt = strDt;
        this.endDt = endDt;
        this.useDays = useDays;
        this.preYrUsed = preYrUsed;
        this.currYrUsed = currYrUsed;
        this.reason = reason;
        this.atchFileNm = atchFileNm;
        this.stsCd = stsCd;
        this.delYn = "N";
    }

    public static LeaveReq create(
            String tenantId, String coId, String empId,
            String leaveTpCd, String deductTpCd, String leaveUnitCd,
            LocalDate strDt, LocalDate endDt,
            BigDecimal useDays, BigDecimal preYrUsed, BigDecimal currYrUsed,
            String reason, String atchFileNm, String stsCd
    ) {
        return new LeaveReq(
                tenantId, coId, empId,
                leaveTpCd, deductTpCd, leaveUnitCd,
                strDt, endDt,
                useDays, preYrUsed, currYrUsed,
                reason, atchFileNm, stsCd
        );
    }

    public void update(
            String leaveTpCd, String deductTpCd, String leaveUnitCd,
            LocalDate strDt, LocalDate endDt,
            BigDecimal useDays, BigDecimal preYrUsed, BigDecimal currYrUsed,
            String reason, String atchFileNm
    ) {
        this.leaveTpCd = leaveTpCd;
        this.deductTpCd = deductTpCd;
        this.leaveUnitCd = leaveUnitCd;
        this.strDt = strDt;
        this.endDt = endDt;
        this.useDays = useDays;
        this.preYrUsed = preYrUsed;
        this.currYrUsed = currYrUsed;
        this.reason = reason;
        this.atchFileNm = atchFileNm;
    }

    public void changeStatus(String stsCd) {
        this.stsCd = stsCd;
    }

    public void reject(String stsCd, String rejReason) {
        this.stsCd = stsCd;
        this.rejReason = rejReason;
    }

    public void softDelete() {
        this.delYn = "Y";
    }

    public boolean isDeleted() {
        return "Y".equals(this.delYn);
    }
}
