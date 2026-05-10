package com.sinwoo.platform.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.List;

public record CreateUsrRequest(
        @NotNull
        Long tenantId,

        Long coId,

        @NotBlank
        @Size(max = 100)
        @Pattern(regexp = "^[A-Za-z0-9][A-Za-z0-9._-]*$", message = "lgnId must contain only letters, numbers, dot, hyphen, or underscore")
        String lgnId,

        @NotBlank
        @Email
        @Size(max = 255)
        String eml,

        @NotBlank
        @Size(min = 8, max = 100)
        String pwd,

        @NotBlank
        @Size(max = 255)
        String dspNm,

        @NotBlank
        @Size(max = 10)
        String loclCd,

        @Size(max = 30)
        String telNo,

        @Size(max = 20)
        String authGrpCd,

        @Size(max = 20)
        String authLvlCd,

        @Size(max = 20)
        String stsCd,

        List<String> roleCds
) {
}
