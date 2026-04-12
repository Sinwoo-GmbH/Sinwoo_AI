package com.sinwoo.attendance.dto;

public record AttendanceCalendarDayResponse(
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
