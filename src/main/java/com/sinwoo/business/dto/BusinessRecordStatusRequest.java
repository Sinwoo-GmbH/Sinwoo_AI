package com.sinwoo.business.dto;

public record BusinessRecordStatusRequest(
        String aprvStsCd,
        String rejectReason
) {
}
