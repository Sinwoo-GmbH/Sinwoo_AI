package com.sinwoo.leave.dto;

import java.math.BigDecimal;

public record LeaveBalanceResponse(
        BigDecimal availableDays,
        BigDecimal afterRequestDays,
        BigDecimal previousYearDays,
        BigDecimal currentYearDays
) {
}
