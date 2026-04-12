package com.sinwoo.worklocation.dto;

import java.util.List;

public record WorkLocationListResponse(
        int totCnt,
        List<WorkLocationResponse> itemList
) {
}
