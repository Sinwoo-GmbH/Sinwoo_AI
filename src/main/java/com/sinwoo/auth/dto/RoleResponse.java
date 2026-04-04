package com.sinwoo.auth.dto;

import com.sinwoo.auth.domain.Role;
import java.time.OffsetDateTime;

public record RoleResponse(
        Long roleId,
        String roleCd,
        String roleNm,
        String roleScopeCd,
        String roleD1Cd,
        String roleD2Cd,
        String roleD3Cd,
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
                role.getRoleScopeCd(),
                role.getRoleD1Cd(),
                role.getRoleD2Cd(),
                role.getRoleD3Cd(),
                role.getRoleGrpCd(),
                role.getRoleLvlCd(),
                role.getCreatedAt(),
                role.getUpdatedAt()
        );
    }
}
