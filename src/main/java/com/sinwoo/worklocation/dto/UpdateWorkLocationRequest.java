package com.sinwoo.worklocation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateWorkLocationRequest(
        @NotNull
        Long tenantId,

        @NotNull
        Long coId,

        @NotBlank
        @Size(max = 255)
        String workLocNm,

        @Size(max = 255)
        String clntCoNm,

        @NotBlank
        @Size(max = 10)
        String ctryCd,

        @NotBlank
        @Size(max = 20)
        String regionCd,

        @Size(max = 255)
        String cityNm,

        @Size(max = 255)
        String addr1,

        @Size(max = 20)
        String stsCd
) {
}
