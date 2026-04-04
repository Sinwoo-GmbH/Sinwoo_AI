package com.sinwoo.menu.dto;

public record RoleMenuAuthResponse(
        String roleCd,
        String roleNm,
        String mnuCd,
        String mnuNm,
        String viewYn,
        String crtYn,
        String updYn,
        String delYn,
        String aprvYn,
        String exprtYn
) {
}
