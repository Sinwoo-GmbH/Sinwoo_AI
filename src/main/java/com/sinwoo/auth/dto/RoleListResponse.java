package com.sinwoo.auth.dto;

import java.util.List;

public record RoleListResponse(
        long totCnt,
        List<RoleResponse> itemList
) {
}
