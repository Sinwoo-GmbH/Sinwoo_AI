package com.sinwoo.platform.worklocation.dto;

import java.util.List;

public record WorkLocListResponse(
        int totCnt,
        List<WorkLocResponse> itemList
) {
}
