package com.sinwoo.attendance.dto;

import java.util.List;

public record AttendanceWidgetResponse(
        String yearMonth,
        String regionCd,
        AttendanceWidgetPolicyResponse policy,
        AttendanceTodayResponse today,
        AttendanceMonthSummaryResponse summary,
        AttendanceHolidayFocusResponse holidayFocus,
        List<AttendanceCalendarDayResponse> dayList
) {
}
