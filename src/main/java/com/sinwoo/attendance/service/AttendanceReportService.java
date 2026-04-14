package com.sinwoo.attendance.service;

import com.sinwoo.attendance.dto.AttendanceReportExportFile;
import com.sinwoo.attendance.dto.AttendanceWorkTimeFilterOptionsResponse;
import com.sinwoo.attendance.dto.AttendanceWorkTimeHistoryListResponse;
import com.sinwoo.attendance.dto.AttendanceWorkTimeHistoryQuery;
import com.sinwoo.common.security.AuthenticatedUser;
import java.util.Locale;

public interface AttendanceReportService {

    AttendanceWorkTimeHistoryListResponse getWorkTimeHistory(
            AuthenticatedUser authenticatedUser,
            AttendanceWorkTimeHistoryQuery query,
            Locale locale
    );

    AttendanceWorkTimeFilterOptionsResponse getWorkTimeFilterOptions(AuthenticatedUser authenticatedUser);

    AttendanceReportExportFile exportWorkTimeHistoryExcel(
            AuthenticatedUser authenticatedUser,
            AttendanceWorkTimeHistoryQuery query,
            Locale locale
    );

    AttendanceReportExportFile exportWorkTimeHistoryPdf(
            AuthenticatedUser authenticatedUser,
            AttendanceWorkTimeHistoryQuery query,
            Locale locale
    );
}
