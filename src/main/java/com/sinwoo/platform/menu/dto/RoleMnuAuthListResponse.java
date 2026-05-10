package com.sinwoo.platform.mnu.dto;

import java.util.List;

public record RoleMnuAuthListResponse(
        long totCnt,
        List<RoleMnuAuthResponse> itemList
) {
}
