package com.sinwoo.business.leave.dto;

import java.math.BigDecimal;

public record LeaveBalResponse(
        BigDecimal availableDays,
        BigDecimal afterRequestDays,
        BigDecimal previousYearDays,
        BigDecimal currentYearDays
) {
}
