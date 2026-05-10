package com.sinwoo.leave.dto;

import java.math.BigDecimal;
import java.util.List;

public record LeaveCalculateResponse(
        String resultCd,
        String resultMessage,
        BigDecimal previousYearDays,
        BigDecimal currentYearDays,
        BigDecimal days,
        BigDecimal afterRequestDays,
        List<LeaveDuplicateResponse> duplicates
) {
}
