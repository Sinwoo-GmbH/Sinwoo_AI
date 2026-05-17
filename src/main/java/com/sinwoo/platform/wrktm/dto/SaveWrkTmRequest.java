package com.sinwoo.platform.wrktm.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalTime;

public record SaveWrkTmRequest(
        @NotNull LocalDate workDt,
        LocalTime strTm,
        LocalTime endTm,
        @Size(max = 500) String rmk
) {
}
