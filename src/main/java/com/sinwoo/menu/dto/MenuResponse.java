package com.sinwoo.menu.dto;

import com.sinwoo.menu.domain.Menu;
import java.time.OffsetDateTime;

public record MenuResponse(
        Long mnuId,
        String mnuCd,
        String mnuNm,
        String mnuScopeCd,
        Long upMnuId,
        String pathUri,
        String iconNm,
        Integer dspOrd,
        String useYn,
        OffsetDateTime crtDtm,
        OffsetDateTime updDtm
) {
    public static MenuResponse from(Menu menu) {
        return new MenuResponse(
                menu.getId(),
                menu.getMnuCd(),
                menu.getMnuNm(),
                menu.getMnuScopeCd(),
                menu.getUpMnuId(),
                menu.getPathUri(),
                menu.getIconNm(),
                menu.getDspOrd(),
                menu.getUseYn(),
                menu.getCreatedAt(),
                menu.getUpdatedAt()
        );
    }
}
