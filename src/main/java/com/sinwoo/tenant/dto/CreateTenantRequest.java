package com.sinwoo.tenant.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateTenantRequest(
        @NotBlank
        @Size(max = 100)
        @Pattern(regexp = "^[A-Za-z0-9][A-Za-z0-9_-]*$", message = "code must contain only letters, numbers, hyphen, or underscore")
        String code,

        @NotBlank
        @Size(max = 255)
        String name
) {
}
