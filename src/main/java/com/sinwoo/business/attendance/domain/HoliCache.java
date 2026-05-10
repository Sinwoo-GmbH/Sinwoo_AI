package com.sinwoo.business.attendance.domain;

import com.sinwoo.common.util.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
        name = "TB_HOLI",
        uniqueConstraints = {
                @UniqueConstraint(name = "UK_TB_HOLI_CTRY_CD_REGION_CD_HOLI_DT", columnNames = {"CTRY_CD", "REGION_CD", "HOLI_DT"})
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class HoliCache extends BaseEntity {

    @Column(name = "CTRY_CD", nullable = false, length = 10)
    private String ctryCd;

    @Column(name = "REGION_CD", nullable = false, length = 20)
    private String regionCd;

    @Column(name = "HOLI_DT", nullable = false)
    private LocalDate holiDt;

    @Column(name = "HOLI_NM_LOC", nullable = false, length = 255)
    private String holiNmLoc;

    @Column(name = "HOLI_NM_EN", nullable = false, length = 255)
    private String holiNmEn;

    @Column(name = "GLOB_YN", nullable = false, length = 1, columnDefinition = "CHAR(1)")
    private String globYn;

    @Column(name = "SRC_NM", nullable = false, length = 50)
    private String srcNm;

    private HoliCache(
            String ctryCd,
            String regionCd,
            LocalDate holiDt,
            String holiNmLoc,
            String holiNmEn,
            String globYn,
            String srcNm
    ) {
        this.ctryCd = ctryCd;
        this.regionCd = regionCd;
        this.holiDt = holiDt;
        this.holiNmLoc = holiNmLoc;
        this.holiNmEn = holiNmEn;
        this.globYn = globYn;
        this.srcNm = srcNm;
    }

    public static HoliCache create(
            String ctryCd,
            String regionCd,
            LocalDate holiDt,
            String holiNmLoc,
            String holiNmEn,
            String globYn,
            String srcNm
    ) {
        return new HoliCache(ctryCd, regionCd, holiDt, holiNmLoc, holiNmEn, globYn, srcNm);
    }

    public void refresh(String holiNmLoc, String holiNmEn, String globYn, String srcNm) {
        this.holiNmLoc = holiNmLoc;
        this.holiNmEn = holiNmEn;
        this.globYn = globYn;
        this.srcNm = srcNm;
    }
}
