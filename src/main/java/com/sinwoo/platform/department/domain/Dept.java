package com.sinwoo.platform.dept.domain;

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
        name = "TB_DEPT",
        uniqueConstraints = {
                @UniqueConstraint(name = "UK_TB_DEPT_TENANT_ID_CO_ID_DEPT_CD", columnNames = {"TENANT_ID", "CO_ID", "DEPT_CD"})
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Dept extends BaseEntity {

    @Column(name = "TENANT_ID", nullable = false)
    private Long tenantId;

    @Column(name = "CO_ID", nullable = false)
    private Long coId;

    @Column(name = "DEPT_CD", nullable = false, length = 100)
    private String deptCd;

    @Column(name = "DEPT_NM", nullable = false, length = 255)
    private String deptNm;

    @Column(name = "UP_DEPT_ID")
    private Long upDeptId;

    @Column(name = "DEPT_LVL_NO", nullable = false)
    private Integer deptLvlNo;

    @Column(name = "STS_CD", nullable = false, length = 20)
    private String stsCd;

    private Dept(Long tenantId, Long coId, String deptCd, String deptNm, Long upDeptId, Integer deptLvlNo, String stsCd) {
        this.tenantId = tenantId;
        this.coId = coId;
        this.deptCd = deptCd;
        this.deptNm = deptNm;
        this.upDeptId = upDeptId;
        this.deptLvlNo = deptLvlNo;
        this.stsCd = stsCd;
    }

    public static Dept create(
            Long tenantId,
            Long coId,
            String deptCd,
            String deptNm,
            Long upDeptId,
            Integer deptLvlNo,
            String stsCd
    ) {
        return new Dept(tenantId, coId, deptCd, deptNm, upDeptId, deptLvlNo, stsCd);
    }
}
