package com.sinwoo.platform.code.dto;

import java.util.List;

public record CdGroupListResponse(
        long totCnt,
        List<CdGroupResponse> itemList
) {
}
