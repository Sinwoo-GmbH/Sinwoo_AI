package com.sinwoo.platform.code.dto;

import java.util.List;

public record CommonCdListResponse(
        long totCnt,
        List<CommonCdResponse> itemList
) {
}
