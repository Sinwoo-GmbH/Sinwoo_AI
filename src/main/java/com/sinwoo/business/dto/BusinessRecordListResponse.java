package com.sinwoo.business.dto;

import java.util.List;
import java.util.Map;

public record BusinessRecordListResponse(
        String moduleCd,
        String tableNm,
        boolean creatableYn,
        boolean editableYn,
        boolean deletableYn,
        List<BusinessRecordColumnResponse> columnList,
        List<Map<String, Object>> itemList,
        long totCnt,
        int page,
        int size
) {
}
