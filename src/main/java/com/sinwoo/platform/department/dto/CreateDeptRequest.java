package com.sinwoo.platform.dept.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateDeptRequest(
        @NotNull
        Long tenantId,

        @NotNull
        Long coId,

        @NotBlank
        @Size(max = 100)
        @Pattern(regexp = "^[A-Za-z0-9][A-Za-z0-9_-]*$", message = "deptCd must contain only letters, numbers, hyphen, or underscore")
        String deptCd,

        @NotBlank
        @Size(max = 255)
        String deptNm,

        Long upDeptId,

        @Size(max = 20)
        String stsCd
) {
}
