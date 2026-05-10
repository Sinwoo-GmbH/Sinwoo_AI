package com.sinwoo.platform.mnu.dto;

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
