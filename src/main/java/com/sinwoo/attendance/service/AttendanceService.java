package com.sinwoo.attendance.service;

import com.sinwoo.attendance.dto.AttendanceManualEntryRequest;
import com.sinwoo.attendance.dto.AttendanceTodayResponse;
import com.sinwoo.attendance.dto.AttendanceWidgetResponse;
import com.sinwoo.common.security.AuthenticatedUser;
import java.time.YearMonth;
import java.util.Locale;

public interface AttendanceService {

    AttendanceWidgetResponse getAttendanceWidget(AuthenticatedUser authenticatedUser, YearMonth yearMonth, Locale locale);

    AttendanceTodayResponse checkIn(AuthenticatedUser authenticatedUser, Locale locale);

    AttendanceTodayResponse checkOut(AuthenticatedUser authenticatedUser, Locale locale);

    AttendanceTodayResponse saveManualEntry(
            AuthenticatedUser authenticatedUser,
            AttendanceManualEntryRequest request,
            Locale locale
    );
}
