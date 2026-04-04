package com.sinwoo.company.domain;

import com.sinwoo.common.domain.BaseEntity;
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
        name = "TB_CO",
        uniqueConstraints = {
                @UniqueConstraint(name = "UK_TB_CO_TENANT_ID_CO_CD", columnNames = {"TENANT_ID", "CO_CD"})
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Company extends BaseEntity {

    @Column(name = "TENANT_ID", nullable = false)
    private Long tenantId;

    @Column(name = "CO_CD", nullable = false, length = 100)
    private String coCd;

    @Column(name = "CO_NM", nullable = false, length = 255)
    private String coNm;

    @Column(name = "REG_NO", length = 100)
    private String regNo;

    @Column(name = "STS_CD", nullable = false, length = 20)
    private String stsCd;

    private Company(Long tenantId, String coCd, String coNm, String regNo, String stsCd) {
        this.tenantId = tenantId;
        this.coCd = coCd;
        this.coNm = coNm;
        this.regNo = regNo;
        this.stsCd = stsCd;
    }

    public static Company create(Long tenantId, String coCd, String coNm, String regNo, String stsCd) {
        return new Company(tenantId, coCd, coNm, regNo, stsCd);
    }
}
