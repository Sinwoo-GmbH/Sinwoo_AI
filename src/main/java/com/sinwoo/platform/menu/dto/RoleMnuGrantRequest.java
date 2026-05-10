package com.sinwoo.platform.mnu.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RoleMnuGrantRequest(
        @NotBlank
        @Size(max = 100)
        String mnuCd,

        @Size(max = 1)
        String viewYn,

        @Size(max = 1)
        String crtYn,

        @Size(max = 1)
        String updYn,

        @Size(max = 1)
        String delYn,

        @Size(max = 1)
        String aprvYn,

        @Size(max = 1)
        String exprtYn
) {
}
