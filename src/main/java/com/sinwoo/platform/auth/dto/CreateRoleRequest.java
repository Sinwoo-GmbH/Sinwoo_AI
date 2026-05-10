package com.sinwoo.platform.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateRoleRequest(
        @NotBlank
        @Size(max = 100)
        @Pattern(regexp = "^ROLE_[A-Z0-9_]+$", message = "roleCd must follow ROLE_* format")
        String roleCd,

        @NotBlank
        @Size(max = 255)
        String roleNm,

        @Size(max = 20)
        String roleScopeCd,

        @Size(max = 30)
        String roleD1Cd,

        @Size(max = 30)
        String roleD2Cd,

        @Size(max = 30)
        String roleD3Cd,

        @Size(max = 20)
        String roleGrpCd,

        @Size(max = 20)
        String roleLvlCd
) {
}
