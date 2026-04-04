package com.sinwoo.menu.dto;

import java.util.List;

public record MenuListResponse(
        long totCnt,
        List<MenuResponse> itemList
) {
}
