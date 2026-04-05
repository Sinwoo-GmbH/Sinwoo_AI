package com.sinwoo.menu.dto;

import com.sinwoo.menu.domain.Menu;
import java.util.ArrayList;
import java.util.List;

public record MenuNodeResponse(
        Long mnuId,
        String mnuCd,
        String mnuNmCd,
        String mnuNm,
        String mnuScopeCd,
        Long upMnuId,
        String pathUri,
        String iconNm,
        Integer dspOrd,
        String billGateCd,
        List<MenuNodeResponse> childList
) {
    public static MenuNodeResponse from(Menu menu, String resolvedMnuNm) {
        return new MenuNodeResponse(
                menu.getId(),
                menu.getMnuCd(),
                menu.getMnuNmCd(),
                resolvedMnuNm,
                menu.getMnuScopeCd(),
                menu.getUpMnuId(),
                menu.getPathUri(),
                menu.getIconNm(),
                menu.getDspOrd(),
                menu.getBillGateCd(),
                new ArrayList<>()
        );
    }
}
