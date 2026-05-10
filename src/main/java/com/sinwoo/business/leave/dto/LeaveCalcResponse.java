package com.sinwoo.business.leave.dto;

import java.math.BigDecimal;
import java.util.List;

public record LeaveCalcResponse(
        String resultCd,
        String resultMsg,
        BigDecimal previousYearDays,
        BigDecimal currentYearDays,
        BigDecimal days,
        BigDecimal afterRequestDays,
        List<LeaveDupResponse> duplicates
) {
}
