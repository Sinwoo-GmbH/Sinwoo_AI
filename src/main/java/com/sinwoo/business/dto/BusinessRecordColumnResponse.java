package com.sinwoo.business.dto;

public record BusinessRecordColumnResponse(
        String columnNm,
        String label,
        String dataTpCd,
        boolean keyYn,
        boolean visibleYn,
        boolean writableYn,
        boolean requiredYn,
        int dspOrd
) {
}
