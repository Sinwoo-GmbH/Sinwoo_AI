package com.sinwoo.platform.user.dto;

import com.sinwoo.platform.user.domain.Usr;
import java.time.OffsetDateTime;
import java.util.List;

public record UsrResponse(
        Long usrId,
        Long tenantId,
        Long coId,
        String lgnId,
        String eml,
        String dspNm,
        String loclCd,
        String telNo,
        String authGrpCd,
        String authLvlCd,
        String stsCd,
        List<String> roleCds,
        OffsetDateTime crtDtm,
        OffsetDateTime updDtm
) {
    public static UsrResponse from(Usr user, List<String> roleCds) {
        return new UsrResponse(
                user.getId(),
                user.getTenantId(),
                user.getCoId(),
                user.getLgnId(),
                user.getEml(),
                user.getDspNm(),
                user.getLoclCd(),
                user.getTelNo(),
                user.getAuthGrpCd(),
                user.getAuthLvlCd(),
                user.getStsCd(),
                roleCds,
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}
