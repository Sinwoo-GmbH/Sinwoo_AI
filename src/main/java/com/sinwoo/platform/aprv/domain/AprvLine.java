package com.sinwoo.platform.aprv.domain;

import com.sinwoo.common.util.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "TB_APRV_LINE")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AprvLine extends BaseEntity {

    @Column(name = "TENANT_ID", nullable = false, length = 20)
    private String tenantId;

    @Column(name = "CO_ID", nullable = false, length = 20)
    private String coId;

    @Column(name = "REQ_TP_CD", nullable = false, length = 20)
    private String reqTpCd;

    @Column(name = "REQ_ID", nullable = false)
    private Long reqId;

    @Column(name = "APRV_TP_CD", nullable = false, length = 10)
    private String aprvTpCd;

    @Column(name = "EMP_ID", nullable = false, length = 20)
    private String empId;

    @Column(name = "STEP_ORDER", nullable = false)
    private Integer stepOrder;

    @Column(name = "STS_CD", nullable = false, length = 20)
    private String stsCd;

    @Column(name = "FINAL_YN", nullable = false, columnDefinition = "CHAR(1)")
    private String finalYn;

    @Column(name = "DEL_YN", nullable = false, columnDefinition = "CHAR(1)")
    private String delYn;

    private AprvLine(
            String tenantId, String coId,
            String reqTpCd, Long reqId,
            String aprvTpCd, String empId,
            Integer stepOrder, String stsCd, String finalYn
    ) {
        this.tenantId = tenantId;
        this.coId = coId;
        this.reqTpCd = reqTpCd;
        this.reqId = reqId;
        this.aprvTpCd = aprvTpCd;
        this.empId = empId;
        this.stepOrder = stepOrder;
        this.stsCd = stsCd;
        this.finalYn = finalYn;
        this.delYn = "N";
    }

    public static AprvLine create(
            String tenantId, String coId,
            String reqTpCd, Long reqId,
            String aprvTpCd, String empId,
            Integer stepOrder, String stsCd, String finalYn
    ) {
        return new AprvLine(
                tenantId, coId,
                reqTpCd, reqId,
                aprvTpCd, empId,
                stepOrder, stsCd, finalYn
        );
    }

    public void changeStatus(String stsCd) {
        this.stsCd = stsCd;
    }

    public void softDelete() {
        this.delYn = "Y";
    }

    public boolean isDeleted() {
        return "Y".equals(this.delYn);
    }

    public boolean isFinalApprover() {
        return "Y".equals(this.finalYn);
    }
}
