package com.sinwoo.code.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateCommonCodeRequest(
        @NotBlank
        @Size(max = 100)
        String grpCd,

        @NotBlank
        @Size(max = 100)
        String cd,

        @Size(max = 255)
        String cdNmKo,

        @NotBlank
        @Size(max = 255)
        String cdNmEn,

        @Size(max = 255)
        String cdNmDe,

        String cdDescKo,

        String cdDescEn,

        String cdDescDe,

        @Size(max = 1)
        String useYn,

        Integer dspOrd
) {
}
