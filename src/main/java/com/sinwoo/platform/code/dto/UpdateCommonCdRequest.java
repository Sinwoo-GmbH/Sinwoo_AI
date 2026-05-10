package com.sinwoo.platform.code.dto;

import jakarta.validation.constraints.Size;

public record UpdateCommonCdRequest(
        @Size(max = 255)
        String cdNmKo,

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
