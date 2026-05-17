package com.sinwoo.platform.wrktm.dto;

import java.util.List;

public record WrkTmListResponse(
        int totCnt,
        List<WrkTmResponse> itemList
) {
}
