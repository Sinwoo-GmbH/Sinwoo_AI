package com.sinwoo.business.attendance.dto;

import java.util.List;

public record AttndWidgetResponse(
        String yearMonth,
        String regionCd,
        AttndWidgetPolicyResponse policy,
        AttndTodayResponse today,
        AttndMonthSummaryResponse summary,
        AttndHoliFocusResponse holidayFocus,
        List<AttndCalDayResponse> dayList
) {
}
