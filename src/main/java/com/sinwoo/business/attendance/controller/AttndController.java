package com.sinwoo.business.attendance.controller;

import com.sinwoo.business.attendance.dto.AttndManualEntryRequest;
import com.sinwoo.business.attendance.dto.AttndTodayResponse;
import com.sinwoo.business.attendance.dto.AttndWidgetResponse;
import com.sinwoo.business.attendance.service.AttndService;
import com.sinwoo.platform.auth.support.AuthErrorCd;
import com.sinwoo.common.security.AuthenticatedUsr;
import com.sinwoo.common.exception.ApiException;
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
public class AttndController {

    private final AttndService attndService;

    @GetMapping("/my/widget")
    public AttndWidgetResponse getMyWidget(
            Authentication authentication,
            @RequestParam(required = false) String yearMonth,
            Locale locale
    ) {
        return attndService.getAttndWidget(requireAuthenticatedUsr(authentication), parseYearMonth(yearMonth), locale);
    }

    @PostMapping("/my/check-in")
    public AttndTodayResponse checkIn(Authentication authentication, Locale locale) {
        return attndService.checkIn(requireAuthenticatedUsr(authentication), locale);
    }

    @PostMapping("/my/check-out")
    public AttndTodayResponse checkOut(Authentication authentication, Locale locale) {
        return attndService.checkOut(requireAuthenticatedUsr(authentication), locale);
    }

    @PostMapping("/my/manual-entry")
    public AttndTodayResponse saveManualEntry(
            Authentication authentication,
            @RequestBody AttndManualEntryRequest request,
            Locale locale
    ) {
        return attndService.saveManualEntry(requireAuthenticatedUsr(authentication), request, locale);
    }

    private AuthenticatedUsr requireAuthenticatedUsr(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedUsr authenticatedUsr)) {
            throw new ApiException(
                    AuthErrorCd.AUTH_AUTHENTICATION_REQUIRED.status(),
                    AuthErrorCd.AUTH_AUTHENTICATION_REQUIRED.code(),
                    AuthErrorCd.AUTH_AUTHENTICATION_REQUIRED.message()
            );
        }
        return authenticatedUsr;
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
