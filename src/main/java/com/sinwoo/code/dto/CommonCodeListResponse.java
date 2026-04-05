package com.sinwoo.code.dto;

import java.util.List;

public record CommonCodeListResponse(
        long totCnt,
        List<CommonCodeResponse> itemList
) {
}
