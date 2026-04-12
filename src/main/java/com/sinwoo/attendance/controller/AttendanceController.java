package com.sinwoo.attendance.controller;

import com.sinwoo.attendance.dto.AttendanceManualEntryRequest;
import com.sinwoo.attendance.dto.AttendanceTodayResponse;
import com.sinwoo.attendance.dto.AttendanceWidgetResponse;
import com.sinwoo.attendance.service.AttendanceService;
import com.sinwoo.auth.support.AuthErrorCode;
import com.sinwoo.common.security.AuthenticatedUser;
import com.sinwoo.common.web.ApiException;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/attendance")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;

    @GetMapping("/my/widget")
    public AttendanceWidgetResponse getMyWidget(
            Authentication authentication,
            @RequestParam(required = false) String yearMonth,
            Locale locale
    ) {
        return attendanceService.getAttendanceWidget(requireAuthenticatedUser(authentication), parseYearMonth(yearMonth), locale);
    }

    @PostMapping("/my/check-in")
    public AttendanceTodayResponse checkIn(Authentication authentication, Locale locale) {
        return attendanceService.checkIn(requireAuthenticatedUser(authentication), locale);
    }

    @PostMapping("/my/check-out")
    public AttendanceTodayResponse checkOut(Authentication authentication, Locale locale) {
        return attendanceService.checkOut(requireAuthenticatedUser(authentication), locale);
    }

    @PostMapping("/my/manual-entry")
    public AttendanceTodayResponse saveManualEntry(
            Authentication authentication,
            @RequestBody AttendanceManualEntryRequest request,
            Locale locale
    ) {
        return attendanceService.saveManualEntry(requireAuthenticatedUser(authentication), request, locale);
    }

    private AuthenticatedUser requireAuthenticatedUser(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedUser authenticatedUser)) {
            throw new ApiException(
                    AuthErrorCode.AUTH_AUTHENTICATION_REQUIRED.status(),
                    AuthErrorCode.AUTH_AUTHENTICATION_REQUIRED.code(),
                    AuthErrorCode.AUTH_AUTHENTICATION_REQUIRED.message()
            );
        }
        return authenticatedUser;
    }

    private YearMonth parseYearMonth(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return YearMonth.parse(value);
        } catch (DateTimeParseException exception) {
            throw new ApiException(org.springframework.http.HttpStatus.BAD_REQUEST, "ATTENDANCE_YEAR_MONTH_INVALID", "Year month must follow yyyy-MM");
        }
    }
}
