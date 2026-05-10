package com.sinwoo.platform.code.dto;

import jakarta.validation.constraints.Size;

public record UpdateCdGroupRequest(
        @Size(max = 255)
        String grpNmKo,

        @Size(max = 255)
        String grpNmEn,

        @Size(max = 255)
        String grpNmDe,

        @Size(max = 1)
        String useYn,

        Integer dspOrd
) {
}
