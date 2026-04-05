package com.sinwoo.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record CredentialLoginRequest(
        @NotBlank
        @Email
        @Size(max = 255)
        String eml,

        @NotBlank
        @Size(max = 2048)
        String pwdEnc
) {
}
