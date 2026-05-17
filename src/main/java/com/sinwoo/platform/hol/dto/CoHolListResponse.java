package com.sinwoo.platform.hol.dto;

import java.util.List;

public record CoHolListResponse(
        int totCnt,
        List<CoHolResponse> itemList
) {
}
