package com.sinwoo.platform.wrktm.dto;

import com.sinwoo.platform.wrktm.domain.WrkTm;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;

public record WrkTmResponse(
        Long id,
        Long empId,
        LocalDate workDt,
        LocalTime strTm,
        LocalTime endTm,
        Short workMin,
        String rmk,
        OffsetDateTime crtDtm,
        OffsetDateTime updDtm
) {
    public static WrkTmResponse from(WrkTm wt) {
        return new WrkTmResponse(
                wt.getId(),
                wt.getEmpId(),
                wt.getWorkDt(),
                wt.getStrTm(),
                wt.getEndTm(),
                wt.getWorkMin(),
                wt.getRmk(),
                wt.getCreatedAt(),
                wt.getUpdatedAt()
        );
    }
}
