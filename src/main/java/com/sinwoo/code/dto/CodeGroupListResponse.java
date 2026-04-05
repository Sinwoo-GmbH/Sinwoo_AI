package com.sinwoo.code.dto;

import java.util.List;

public record CodeGroupListResponse(
        long totCnt,
        List<CodeGroupResponse> itemList
) {
}
