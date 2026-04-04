package com.sinwoo.company.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateCompanyRequest(
        @NotNull
        Long tenantId,

        @NotBlank
        @Size(max = 100)
        @Pattern(regexp = "^[A-Za-z0-9][A-Za-z0-9_-]*$", message = "coCd must contain only letters, numbers, hyphen, or underscore")
        String coCd,

        @NotBlank
        @Size(max = 255)
        String coNm,

        @Size(max = 100)
        String regNo,

        @Size(max = 20)
        String stsCd
) {
}
