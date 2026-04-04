package com.sinwoo.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CredentialLoginRequest(
        @NotBlank
        @Size(max = 100)
        String tenantCd,

        @NotBlank
        @Size(max = 100)
        @Pattern(regexp = "^[A-Za-z0-9][A-Za-z0-9._-]*$", message = "lgnId must contain only letters, numbers, dot, hyphen, or underscore")
        String lgnId,

        @NotBlank
        @Size(min = 8, max = 100)
        String pwd
) {
}
