package com.sinwoo.platform.menu.dto;

import java.util.List;

public record RoleMnuAuthListResponse(
        long totCnt,
        List<RoleMnuAuthResponse> itemList
) {
}
