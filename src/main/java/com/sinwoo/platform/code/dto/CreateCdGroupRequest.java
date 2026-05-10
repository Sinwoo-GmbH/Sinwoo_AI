package com.sinwoo.platform.code.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateCdGroupRequest(
        @NotBlank
        @Size(max = 100)
        String grpCd,

        @Size(max = 255)
        String grpNmKo,

        @NotBlank
        @Size(max = 255)
        String grpNmEn,

        @Size(max = 255)
        String grpNmDe,

        @Size(max = 1)
        String sysYn,

        @Size(max = 1)
        String useYn,

        Integer dspOrd
) {
}
