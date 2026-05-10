package com.sinwoo.business.dto;

public record BusinessRecordQuery(
        String keyword,
        String yearMonth,
        int page,
        int size
) {
}
