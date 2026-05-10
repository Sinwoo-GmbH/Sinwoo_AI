package com.sinwoo.business.dto;

import java.util.List;

public record BusinessRelatedListResponse(
        String moduleCd,
        Long recordId,
        List<BusinessRelatedTableResponse> tableList
) {
}
