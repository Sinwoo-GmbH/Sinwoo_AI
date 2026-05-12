package com.sinwoo.platform.menu.dto;

public record RoleMnuAuthResponse(
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
