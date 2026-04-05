package com.sinwoo.auth.dto;

import java.util.List;

public record CurrentUserResponse(
        Long usrId,
        Long tenantId,
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
