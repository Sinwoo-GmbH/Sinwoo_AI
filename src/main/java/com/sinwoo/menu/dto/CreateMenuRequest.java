package com.sinwoo.menu.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateMenuRequest(
        @NotBlank
        @Size(max = 100)
        String mnuCd,

        @NotBlank
        @Size(max = 255)
        String mnuNm,

        @NotBlank
        @Size(max = 20)
        String mnuScopeCd,

        Long upMnuId,

        @Size(max = 500)
        String pathUri,

        @Size(max = 100)
        String iconNm,

        Integer dspOrd,

        @Size(max = 1)
        String useYn
) {
}
