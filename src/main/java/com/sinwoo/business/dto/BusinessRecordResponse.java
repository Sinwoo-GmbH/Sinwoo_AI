package com.sinwoo.business.dto;

import java.util.Map;

public record BusinessRecordResponse(
        String moduleCd,
        String tableNm,
        Map<String, Object> values
) {
}
