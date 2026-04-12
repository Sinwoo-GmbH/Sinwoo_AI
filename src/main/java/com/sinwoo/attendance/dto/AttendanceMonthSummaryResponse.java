package com.sinwoo.attendance.dto;

public record AttendanceMonthSummaryResponse(
        int workedDayCnt,
        int holidayCnt,
        int weekendCnt
) {
}
