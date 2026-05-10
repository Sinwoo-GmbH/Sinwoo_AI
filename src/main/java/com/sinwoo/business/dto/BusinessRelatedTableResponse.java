package com.sinwoo.business.dto;

import java.util.List;
import java.util.Map;

public record BusinessRelatedTableResponse(
        String tableNm,
        String label,
        List<String> columnList,
        List<Map<String, Object>> itemList,
        long totCnt
) {
}
