package com.sinwoo.platform.user.dto;

import java.util.List;

public record UsrListResponse(
        long totCnt,
        List<UsrResponse> itemList
) {
}
