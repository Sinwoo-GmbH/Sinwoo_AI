package com.sinwoo.business.attendance.dto;

public record AttndManualEntryRequest(
        String attndDt,
        String chkinTm,
        String chkoutTm,
        String attndStsCd
) {
}
