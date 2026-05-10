package com.sinwoo.business.module.dto;

import java.util.List;

public record BizRelListResponse(
        String modCd,
        Long recId,
        List<BizRelTableResponse> tableList
) {
}
