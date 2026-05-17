package com.sinwoo.platform.hol.dto;

import java.util.List;

public record RgnHolListResponse(
        int totCnt,
        List<RgnHolResponse> itemList
) {
}
