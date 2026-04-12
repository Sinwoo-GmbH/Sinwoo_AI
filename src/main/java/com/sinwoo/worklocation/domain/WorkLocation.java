package com.sinwoo.worklocation.domain;

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
        name = "TB_WORK_LOC",
        uniqueConstraints = {
                @UniqueConstraint(name = "UK_TB_WORK_LOC_TENANT_ID_CO_ID_WORK_LOC_CD", columnNames = {"TENANT_ID", "CO_ID", "WORK_LOC_CD"})
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WorkLocation extends BaseEntity {

    @Column(name = "TENANT_ID", nullable = false)
    private Long tenantId;

    @Column(name = "CO_ID", nullable = false)
    private Long coId;

    @Column(name = "WORK_LOC_CD", nullable = false, length = 100)
    private String workLocCd;

    @Column(name = "WORK_LOC_NM", nullable = false, length = 255)
    private String workLocNm;

    @Column(name = "CLNT_CO_NM", length = 255)
    private String clntCoNm;

    @Column(name = "CTRY_CD", nullable = false, length = 10)
    private String ctryCd;

    @Column(name = "REGION_CD", nullable = false, length = 20)
    private String regionCd;

    @Column(name = "CITY_NM", length = 255)
    private String cityNm;

    @Column(name = "ADDR_1", length = 255)
    private String addr1;

    @Column(name = "STS_CD", nullable = false, length = 20)
    private String stsCd;

    private WorkLocation(
            Long tenantId,
            Long coId,
            String workLocCd,
            String workLocNm,
            String clntCoNm,
            String ctryCd,
            String regionCd,
            String cityNm,
            String addr1,
            String stsCd
    ) {
        this.tenantId = tenantId;
        this.coId = coId;
        this.workLocCd = workLocCd;
        this.workLocNm = workLocNm;
        this.clntCoNm = clntCoNm;
        this.ctryCd = ctryCd;
        this.regionCd = regionCd;
        this.cityNm = cityNm;
        this.addr1 = addr1;
        this.stsCd = stsCd;
    }

    public static WorkLocation create(
            Long tenantId,
            Long coId,
            String workLocCd,
            String workLocNm,
            String clntCoNm,
            String ctryCd,
            String regionCd,
            String cityNm,
            String addr1,
            String stsCd
    ) {
        return new WorkLocation(tenantId, coId, workLocCd, workLocNm, clntCoNm, ctryCd, regionCd, cityNm, addr1, stsCd);
    }

    public void update(
            String workLocNm,
            String clntCoNm,
            String ctryCd,
            String regionCd,
            String cityNm,
            String addr1,
            String stsCd
    ) {
        this.workLocNm = workLocNm;
        this.clntCoNm = clntCoNm;
        this.ctryCd = ctryCd;
        this.regionCd = regionCd;
        this.cityNm = cityNm;
        this.addr1 = addr1;
        this.stsCd = stsCd;
    }
}
