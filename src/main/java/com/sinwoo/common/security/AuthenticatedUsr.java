package com.sinwoo.common.security;

import java.util.List;

public record AuthenticatedUsr(
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
        List<String> roleCds
) {
}
