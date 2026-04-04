package com.sinwoo.menu.dto;

import com.sinwoo.menu.domain.Menu;
import java.util.ArrayList;
import java.util.List;

public record MenuNodeResponse(
        Long mnuId,
        String mnuCd,
        String mnuNm,
        String mnuScopeCd,
        Long upMnuId,
        String pathUri,
        String iconNm,
        Integer dspOrd,
        List<MenuNodeResponse> childList
) {
    public static MenuNodeResponse from(Menu menu) {
        return new MenuNodeResponse(
                menu.getId(),
                menu.getMnuCd(),
                menu.getMnuNm(),
                menu.getMnuScopeCd(),
                menu.getUpMnuId(),
                menu.getPathUri(),
                menu.getIconNm(),
                menu.getDspOrd(),
                new ArrayList<>()
        );
    }
}
