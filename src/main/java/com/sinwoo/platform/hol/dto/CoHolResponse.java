package com.sinwoo.platform.hol.dto;

import com.sinwoo.platform.hol.domain.CoHol;
import java.time.LocalDate;
import java.time.OffsetDateTime;

public record CoHolResponse(
        Long id,
        String holidayNm,
        LocalDate strDt,
        LocalDate endDt,
        String annualYn,
        Short applyYr,
        OffsetDateTime crtDtm,
        OffsetDateTime updDtm
) {
    public static CoHolResponse from(CoHol h) {
        return new CoHolResponse(
                h.getId(), h.getHolidayNm(), h.getStrDt(), h.getEndDt(),
                h.getAnnualYn(), h.getApplyYr(), h.getCreatedAt(), h.getUpdatedAt()
        );
    }
}
