package com.sinwoo.platform.company.domain;

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
        name = "TB_CO",
        uniqueConstraints = {
                @UniqueConstraint(name = "UK_TB_CO_TENANT_ID_CO_CD", columnNames = {"TENANT_ID", "CO_CD"})
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Co extends BaseEntity {

    @Column(name = "TENANT_ID", nullable = false)
    private Long tenantId;

    @Column(name = "CO_CD", nullable = false, length = 100)
    private String coCd;

    @Column(name = "CO_NM", nullable = false, length = 255)
    private String coNm;

    @Column(name = "REG_NO", length = 100)
    private String regNo;

    @Column(name = "HQ_CTRY_CD", length = 10)
    private String hqCtryCd;

    @Column(name = "HQ_REGION_CD", length = 20)
    private String hqRegionCd;

    @Column(name = "HQ_CITY_NM", length = 255)
    private String hqCityNm;

    @Column(name = "HQ_ADDR_1", length = 255)
    private String hqAddr1;

    @Column(name = "STS_CD", nullable = false, length = 20)
    private String stsCd;

    private Co(
            Long tenantId,
            String coCd,
            String coNm,
            String regNo,
            String hqCtryCd,
            String hqRegionCd,
            String hqCityNm,
            String hqAddr1,
            String stsCd
    ) {
        this.tenantId = tenantId;
        this.coCd = coCd;
        this.coNm = coNm;
        this.regNo = regNo;
        this.hqCtryCd = hqCtryCd;
        this.hqRegionCd = hqRegionCd;
        this.hqCityNm = hqCityNm;
        this.hqAddr1 = hqAddr1;
        this.stsCd = stsCd;
    }

    public static Co create(
            Long tenantId,
            String coCd,
            String coNm,
            String regNo,
            String hqCtryCd,
            String hqRegionCd,
            String hqCityNm,
            String hqAddr1,
            String stsCd
    ) {
        return new Co(tenantId, coCd, coNm, regNo, hqCtryCd, hqRegionCd, hqCityNm, hqAddr1, stsCd);
    }
}
