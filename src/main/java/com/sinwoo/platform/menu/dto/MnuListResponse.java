package com.sinwoo.platform.menu.dto;

import java.util.List;

public record MnuListResponse(
        long totCnt,
        List<MnuResponse> itemList
) {
}
