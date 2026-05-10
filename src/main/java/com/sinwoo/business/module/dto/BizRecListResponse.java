package com.sinwoo.business.module.dto;

import java.util.List;
import java.util.Map;

public record BizRecListResponse(
        String modCd,
        String tblNm,
        boolean creatableYn,
        boolean editableYn,
        boolean deletableYn,
        List<BizRecColResponse> colList,
        List<Map<String, Object>> itemList,
        long totCnt,
        int page,
        int size
) {
}
