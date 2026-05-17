package com.sinwoo.platform.hol.dto;

import com.sinwoo.platform.hol.domain.RgnHol;
import java.time.LocalDate;

public record RgnHolResponse(
        Long id,
        Short yr,
        String regionCd,
        String regionNm,
        LocalDate holidayDt,
        String holidayNm,
        String wkndYn
) {
    public static RgnHolResponse from(RgnHol h) {
        return new RgnHolResponse(
                h.getId(), h.getYr(), h.getRegionCd(), h.getRegionNm(),
                h.getHolidayDt(), h.getHolidayNm(), h.getWkndYn()
        );
    }
}
