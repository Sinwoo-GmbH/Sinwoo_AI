package com.sinwoo.attendance.dto;

public record AttendanceTodayResponse(
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
