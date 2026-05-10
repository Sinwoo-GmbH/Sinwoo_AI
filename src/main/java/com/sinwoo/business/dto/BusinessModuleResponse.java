package com.sinwoo.business.dto;

import java.util.List;

public record BusinessModuleResponse(
        String moduleCd,
        String menuCd,
        String groupCd,
        String moduleNm,
        String description,
        String legacyMenuCd,
        String legacyUri,
        String primaryTableNm,
        List<String> tableNms,
        long itemCnt,
        List<BusinessModuleMetricResponse> metricList
) {
}
