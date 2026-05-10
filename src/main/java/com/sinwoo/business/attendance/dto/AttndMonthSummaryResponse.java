package com.sinwoo.business.attendance.dto;

public record AttndMonthSummaryResponse(
        int workedDayCnt,
        int holidayCnt,
        int weekendCnt
) {
}
