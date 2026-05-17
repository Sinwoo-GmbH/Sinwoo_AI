package com.sinwoo.platform.hol.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record UpdateCoHolRequest(
        @NotBlank @Size(max = 255) String holidayNm,
        @NotNull LocalDate strDt,
        @NotNull LocalDate endDt,
        @NotBlank @Size(max = 1) String annualYn,
        @NotNull Short applyYr
) {
}
