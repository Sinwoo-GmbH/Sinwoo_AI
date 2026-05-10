package com.sinwoo.business.module.dto;

public record BizRecColResponse(
        String colNm,
        String label,
        String dataTpCd,
        boolean keyYn,
        boolean visibleYn,
        boolean writableYn,
        boolean requiredYn,
        int dspOrd
) {
}
