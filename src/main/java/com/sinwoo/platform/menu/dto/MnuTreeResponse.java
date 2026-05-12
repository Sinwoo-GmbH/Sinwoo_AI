package com.sinwoo.platform.menu.dto;

import java.util.List;

public record MnuTreeResponse(
        long totCnt,
        List<MnuNodeResponse> itemList
) {
}
