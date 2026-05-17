package com.sinwoo.platform.employee.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record CreateEmpRequest(
        @NotNull
        Long tenantId,

        @NotNull
        Long coId,

        Long usrId,

        Long deptId,

        Long mgrEmpId,

        @NotBlank
        @Size(max = 100)
        @Pattern(regexp = "^[A-Za-z0-9][A-Za-z0-9_-]*$", message = "empNo must contain only letters, numbers, hyphen, or underscore")
        String empNo,

        @NotBlank
        @Size(max = 255)
        String empNm,

        @Size(max = 20)
        String teamRoleCd,

        @Size(max = 20)
        String jobTtlCd,

        LocalDate hireDt,

        LocalDate retrDt,

        @Size(max = 20)
        String stsCd
) {
}
