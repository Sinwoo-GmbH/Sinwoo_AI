package com.sinwoo.platform.auth.dto;

import java.util.List;

public record RoleListResponse(
        long totCnt,
        List<RoleResponse> itemList
) {
}
