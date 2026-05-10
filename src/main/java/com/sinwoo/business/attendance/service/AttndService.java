package com.sinwoo.business.attendance.service;

import com.sinwoo.business.attendance.dto.AttndManualEntryRequest;
import com.sinwoo.business.attendance.dto.AttndTodayResponse;
import com.sinwoo.business.attendance.dto.AttndWidgetResponse;
import com.sinwoo.common.security.AuthenticatedUsr;
import java.time.YearMonth;
import java.util.Locale;

public interface AttndService {

    AttndWidgetResponse getAttndWidget(AuthenticatedUsr authenticatedUsr, YearMonth yearMonth, Locale locale);

    AttndTodayResponse checkIn(AuthenticatedUsr authenticatedUsr, Locale locale);

    AttndTodayResponse checkOut(AuthenticatedUsr authenticatedUsr, Locale locale);

    AttndTodayResponse saveManualEntry(
            AuthenticatedUsr authenticatedUsr,
            AttndManualEntryRequest request,
            Locale locale
    );
}
