package com.sinwoo.business.module.dto;

import java.util.List;

public record BizModResponse(
        String modCd,
        String mnuCd,
        String groupCd,
        String modNm,
        String desc,
        String legacyMnuCd,
        String legacyUri,
        String primaryTblNm,
        List<String> tblNms,
        long itemCnt,
        List<BizModMetricResponse> metricList
) {
}
