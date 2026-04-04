package com.sinwoo.user.dto;

import com.sinwoo.user.domain.User;
import java.time.OffsetDateTime;
import java.util.List;

public record UserResponse(
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
    public static UserResponse from(User user, List<String> roleCds) {
        return new UserResponse(
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
