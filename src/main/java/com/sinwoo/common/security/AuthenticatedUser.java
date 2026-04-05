package com.sinwoo.common.security;

import java.util.List;

public record AuthenticatedUser(
        Long usrId,
        Long tenantId,
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
