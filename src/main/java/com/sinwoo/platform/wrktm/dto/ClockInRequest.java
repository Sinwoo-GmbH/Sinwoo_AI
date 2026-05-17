package com.sinwoo.platform.wrktm.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;

public record ClockInRequest(
        @NotNull LocalDate workDt,
        @NotNull LocalTime strTm
) {
}
