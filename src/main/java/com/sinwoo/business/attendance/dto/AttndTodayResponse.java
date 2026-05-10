package com.sinwoo.business.attendance.dto;

public record AttndTodayResponse(
        String attndDt,
        String attndStsCd,
        String attndStsNm,
        String chkinDtm,
        String chkoutDtm,
        boolean checkInAvailYn,
        boolean checkOutAvailYn,
        boolean holidayYn,
        String holidayNm
) {
}
