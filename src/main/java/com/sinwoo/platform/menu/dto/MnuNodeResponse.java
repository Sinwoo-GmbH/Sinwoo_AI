package com.sinwoo.platform.menu.dto;

import com.sinwoo.platform.menu.domain.Mnu;
import java.util.ArrayList;
import java.util.List;

public record MnuNodeResponse(
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
        List<MnuNodeResponse> childList
) {
    public static MnuNodeResponse from(Mnu mnu, String resolvedMnuNm) {
        return new MnuNodeResponse(
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
                new ArrayList<>()
        );
    }
}
