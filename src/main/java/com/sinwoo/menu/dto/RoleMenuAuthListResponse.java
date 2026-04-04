package com.sinwoo.menu.dto;

import java.util.List;

public record RoleMenuAuthListResponse(
        long totCnt,
        List<RoleMenuAuthResponse> itemList
) {
}
