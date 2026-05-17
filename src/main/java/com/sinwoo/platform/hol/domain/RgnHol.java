package com.sinwoo.platform.hol.domain;

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
        name = "TB_REGION_HOLIDAY",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "UK_TB_REGION_HOLIDAY_YR_RGN_DT",
                        columnNames = {"YR", "REGION_CD", "HOLIDAY_DT"}
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RgnHol extends BaseEntity {

    @Column(name = "YR", nullable = false)
    private Short yr;

    @Column(name = "REGION_CD", nullable = false, length = 20)
    private String regionCd;

    @Column(name = "REGION_NM", length = 100)
    private String regionNm;

    @Column(name = "HOLIDAY_DT", nullable = false)
    private LocalDate holidayDt;

    @Column(name = "HOLIDAY_NM", nullable = false, length = 255)
    private String holidayNm;

    @Column(name = "WKND_YN", nullable = false, columnDefinition = "CHAR(1)")
    private String wkndYn;

    private RgnHol(
            Short yr,
            String regionCd,
            String regionNm,
            LocalDate holidayDt,
            String holidayNm,
            String wkndYn
    ) {
        this.yr = yr;
        this.regionCd = regionCd;
        this.regionNm = regionNm;
        this.holidayDt = holidayDt;
        this.holidayNm = holidayNm;
        this.wkndYn = wkndYn;
    }

    public static RgnHol create(
            Short yr,
            String regionCd,
            String regionNm,
            LocalDate holidayDt,
            String holidayNm,
            String wkndYn
    ) {
        return new RgnHol(yr, regionCd, regionNm, holidayDt, holidayNm, wkndYn);
    }
}
