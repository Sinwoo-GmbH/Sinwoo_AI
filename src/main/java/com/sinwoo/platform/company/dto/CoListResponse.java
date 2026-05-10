package com.sinwoo.platform.company.dto;

import java.util.List;

public record CoListResponse(
        long totCnt,
        List<CoResponse> itemList
) {
}
