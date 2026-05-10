package com.sinwoo.business.module.dto;

import java.util.List;

public record BizModListResponse(
        List<BizModResponse> itemList
) {
}
