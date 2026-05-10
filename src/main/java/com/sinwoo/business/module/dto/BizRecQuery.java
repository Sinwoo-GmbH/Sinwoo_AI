package com.sinwoo.business.module.dto;

public record BizRecQuery(
        String keyword,
        String yearMonth,
        int page,
        int size
) {
}
