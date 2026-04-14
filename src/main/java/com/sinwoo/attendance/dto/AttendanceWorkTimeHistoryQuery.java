package com.sinwoo.attendance.dto;

public record AttendanceWorkTimeHistoryQuery(
        String yearMonth,
        String empNm,
        String deptNm,
        String keyword
) {
}
