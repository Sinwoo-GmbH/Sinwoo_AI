package com.sinwoo.business.module.dto;

import java.util.Map;

public record BizRecResponse(
        String modCd,
        String tblNm,
        Map<String, Object> values
) {
}
