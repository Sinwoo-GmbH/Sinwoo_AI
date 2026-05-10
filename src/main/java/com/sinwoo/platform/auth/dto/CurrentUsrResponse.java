package com.sinwoo.platform.auth.dto;

import java.util.List;

public record CurrentUsrResponse(
        Long usrId,
        Long tenantId,
        String tenantCd,
        Long coId,
        String tenantTpCd,
        String lgnId,
        String eml,
        String dspNm,
        String authGrpCd,
        String authLvlCd,
        List<String> roleCds,
        String billEntitledYn
) {
}
