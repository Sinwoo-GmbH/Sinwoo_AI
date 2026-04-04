package com.sinwoo.tenant.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateTenantRequest(
        @NotBlank
        @Size(max = 100)
        @Pattern(regexp = "^[A-Za-z0-9][A-Za-z0-9_-]*$", message = "tenantCd must contain only letters, numbers, hyphen, or underscore")
        String tenantCd,

        @NotBlank
        @Size(max = 255)
        String tenantNm,

        @Size(max = 20)
        String tenantTpCd,

        @Size(max = 1)
        String billFreeYn
) {
}
