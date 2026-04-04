package com.sinwoo.menu.dto;

import java.util.List;

public record MenuTreeResponse(
        long totCnt,
        List<MenuNodeResponse> itemList
) {
}
