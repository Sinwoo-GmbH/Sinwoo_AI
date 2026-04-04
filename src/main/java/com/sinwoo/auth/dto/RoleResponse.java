package com.sinwoo.auth.dto;

import com.sinwoo.auth.domain.Role;
import java.time.OffsetDateTime;

public record RoleResponse(
        Long roleId,
        String roleCd,
        String roleNm,
        String roleGrpCd,
        String roleLvlCd,
        OffsetDateTime crtDtm,
        OffsetDateTime updDtm
) {
    public static RoleResponse from(Role role) {
        return new RoleResponse(
                role.getId(),
                role.getRoleCd(),
                role.getRoleNm(),
                role.getRoleGrpCd(),
                role.getRoleLvlCd(),
                role.getCreatedAt(),
                role.getUpdatedAt()
        );
    }
}
