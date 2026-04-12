package com.sinwoo.attendance.dto;

public record AttendanceManualEntryRequest(
        String attndDt,
        String chkinTm,
        String chkoutTm,
        String attndStsCd
) {
}
