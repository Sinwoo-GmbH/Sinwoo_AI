package com.sinwoo.business.attendance.dto;

public record AttndCalDayResponse(
        String dt,
        int dayNo,
        boolean inMonthYn,
        boolean todayYn,
        boolean weekendYn,
        boolean holidayYn,
        String holidayNm,
        String attndStsCd,
        String chkinTm,
        String chkoutTm
) {
}
