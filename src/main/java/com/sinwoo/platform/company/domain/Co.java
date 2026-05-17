package com.sinwoo.platform.company.domain;

import com.sinwoo.common.util.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalTime;
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

    @Column(name = "EXP_ACC_CD")
    private Integer expAccCd;

    @Column(name = "COUNTRY", length = 5)
    private String country;

    @Column(name = "ADDRESS", length = 500)
    private String address;

    @Column(name = "STS_CD", nullable = false, length = 20)
    private String stsCd;

    @Column(name = "LUNCH_STR_TM")
    private LocalTime lunchStrTm;

    @Column(name = "LUNCH_END_TM")
    private LocalTime lunchEndTm;

    private Co(
            Long tenantId,
            String coCd,
            String coNm,
            String regNo,
            String stsCd
    ) {
        this.tenantId = tenantId;
        this.coCd = coCd;
        this.coNm = coNm;
        this.regNo = regNo;
        this.stsCd = stsCd;
    }

    public static Co create(
            Long tenantId,
            String coCd,
            String coNm,
            String regNo,
            String stsCd
    ) {
        return new Co(tenantId, coCd, coNm, regNo, stsCd);
    }

    public void updateLunchTime(LocalTime lunchStrTm, LocalTime lunchEndTm) {
        this.lunchStrTm = lunchStrTm;
        this.lunchEndTm = lunchEndTm;
    }
}
