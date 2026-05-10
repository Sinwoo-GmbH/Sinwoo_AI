package com.sinwoo.business.dto;

import java.util.List;

public record BusinessModuleListResponse(
        List<BusinessModuleResponse> itemList
) {
}
