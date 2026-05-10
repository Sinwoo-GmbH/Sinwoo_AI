package com.sinwoo.platform.mnu.dto;

import com.sinwoo.platform.mnu.domain.Mnu;
import java.time.OffsetDateTime;

public record MnuResponse(
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
        String useYn,
        OffsetDateTime crtDtm,
        OffsetDateTime updDtm
) {
    public static MnuResponse from(Mnu mnu, String resolvedMnuNm) {
        return new MnuResponse(
                mnu.getId(),
                mnu.getMnuCd(),
                mnu.getMnuNmCd(),
                resolvedMnuNm,
                mnu.getMnuScopeCd(),
                mnu.getUpMnuId(),
                mnu.getPathUri(),
                mnu.getIconNm(),
                mnu.getDspOrd(),
                mnu.getBillGateCd(),
                mnu.getUseYn(),
                mnu.getCreatedAt(),
                mnu.getUpdatedAt()
        );
    }
}
