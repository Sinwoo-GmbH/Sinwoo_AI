package com.sinwoo.platform.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record CredLoginRequest(
        @NotBlank
        @Email
        @Size(max = 255)
        String eml,

        @NotBlank
        @Size(max = 2048)
        String pwdEnc
) {
}
