package com.sinwoo.business.module.dto;

import java.util.List;
import java.util.Map;

public record BizRelTableResponse(
        String tblNm,
        String label,
        List<String> colList,
        List<Map<String, Object>> itemList,
        long totCnt
) {
}
