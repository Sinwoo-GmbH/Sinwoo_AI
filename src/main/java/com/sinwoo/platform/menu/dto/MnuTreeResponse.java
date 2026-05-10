package com.sinwoo.platform.mnu.dto;

import java.util.List;

public record MnuTreeResponse(
        long totCnt,
        List<MnuNodeResponse> itemList
) {
}
