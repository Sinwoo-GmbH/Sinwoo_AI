package com.sinwoo.platform.expense.domain;

import com.sinwoo.common.util.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
        name = "TB_EXP_ACC",
        uniqueConstraints = {
                @UniqueConstraint(name = "UK_TB_EXP_ACC_TENANT_CO_CD", columnNames = {"TENANT_ID", "CO_ID", "EXP_ACC_CD"})
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ExpAcc extends BaseEntity {

    @Column(name = "TENANT_ID", nullable = false)
    private Long tenantId;

    @Column(name = "CO_ID", nullable = false)
    private Long coId;

    @Column(name = "EXP_ACC_CD", nullable = false)
    private Integer expAccCd;

    @Column(name = "EXP_ACC_DESC", nullable = false, length = 200)
    private String expAccDesc;

    @Column(name = "EXP_ACC_DESC_KO", length = 200)
    private String expAccDescKo;

    @Column(name = "EXP_ACC_DESC_EN", length = 200)
    private String expAccDescEn;

    @Column(name = "PURCHASE_YN", nullable = false, columnDefinition = "CHAR(1)")
    private String purchaseYn;

    @Column(name = "SALES_YN", nullable = false, columnDefinition = "CHAR(1)")
    private String salesYn;

    @Column(name = "EXPENSE_YN", nullable = false, columnDefinition = "CHAR(1)")
    private String expenseYn;

    @Column(name = "FIXED_EXP_YN", nullable = false, columnDefinition = "CHAR(1)")
    private String fixedExpYn;

    @Column(name = "DRCR_CD", nullable = false, columnDefinition = "CHAR(1)")
    private String drcrCd;

    @Column(name = "CARRYOVER_CD", nullable = false, columnDefinition = "CHAR(1)")
    private String carryoverCd;

    @Column(name = "FI_STMT_CD", nullable = false, length = 5)
    private String fiStmtCd;

    @Column(name = "EXP_CATG_CD", length = 10)
    private String expCatgCd;

    @Column(name = "REMARK", length = 500)
    private String remark;

    @Column(name = "DSP_ORD", nullable = false)
    private Integer dspOrd;

    @Column(name = "DEL_YN", nullable = false, columnDefinition = "CHAR(1)")
    private String delYn;
}
