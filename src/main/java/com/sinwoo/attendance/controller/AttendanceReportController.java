package com.sinwoo.attendance.controller;

import com.sinwoo.attendance.dto.AttendanceReportExportFile;
import com.sinwoo.attendance.dto.AttendanceWorkTimeFilterOptionsResponse;
import com.sinwoo.attendance.dto.AttendanceWorkTimeHistoryListResponse;
import com.sinwoo.attendance.dto.AttendanceWorkTimeHistoryQuery;
import com.sinwoo.attendance.service.AttendanceReportService;
import com.sinwoo.auth.support.AuthErrorCode;
import com.sinwoo.common.security.AuthenticatedUser;
import com.sinwoo.common.web.ApiException;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/attendance/reports/work-time")
@RequiredArgsConstructor
public class AttendanceReportController {

    private static final String EXCEL_CONTENT_TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    private final AttendanceReportService attendanceReportService;

    @GetMapping("/history")
    public AttendanceWorkTimeHistoryListResponse getWorkTimeHistory(
            Authentication authentication,
            @RequestParam(required = false) String yearMonth,
            @RequestParam(required = false) String empNm,
            @RequestParam(required = false) String deptNm,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String lang,
            Locale locale
    ) {
        return attendanceReportService.getWorkTimeHistory(
                requireAuthenticatedUser(authentication),
                new AttendanceWorkTimeHistoryQuery(yearMonth, empNm, deptNm, keyword),
                resolveRequestedLocale(lang, locale)
        );
    }

    @GetMapping("/filter-options")
    public AttendanceWorkTimeFilterOptionsResponse getWorkTimeFilterOptions(Authentication authentication) {
        return attendanceReportService.getWorkTimeFilterOptions(requireAuthenticatedUser(authentication));
    }

    @GetMapping("/history/export/excel")
    public ResponseEntity<byte[]> exportWorkTimeHistoryExcel(
            Authentication authentication,
            @RequestParam(required = false) String yearMonth,
            @RequestParam(required = false) String empNm,
            @RequestParam(required = false) String deptNm,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String lang,
            Locale locale
    ) {
        AttendanceReportExportFile file = attendanceReportService.exportWorkTimeHistoryExcel(
                requireAuthenticatedUser(authentication),
                new AttendanceWorkTimeHistoryQuery(yearMonth, empNm, deptNm, keyword),
                resolveRequestedLocale(lang, locale)
        );
        return buildExportResponse(file, EXCEL_CONTENT_TYPE);
    }

    @GetMapping("/history/export/pdf")
    public ResponseEntity<byte[]> exportWorkTimeHistoryPdf(
            Authentication authentication,
            @RequestParam(required = false) String yearMonth,
            @RequestParam(required = false) String empNm,
            @RequestParam(required = false) String deptNm,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String lang,
            Locale locale
    ) {
        AttendanceReportExportFile file = attendanceReportService.exportWorkTimeHistoryPdf(
                requireAuthenticatedUser(authentication),
                new AttendanceWorkTimeHistoryQuery(yearMonth, empNm, deptNm, keyword),
                resolveRequestedLocale(lang, locale)
        );
        return buildExportResponse(file, "application/pdf");
    }

    private ResponseEntity<byte[]> buildExportResponse(AttendanceReportExportFile file, String fallbackContentType) {
        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + file.fileNm().replace("\"", "") + "\"; filename*=UTF-8''"
                                + java.net.URLEncoder.encode(file.fileNm(), StandardCharsets.UTF_8).replace("+", "%20")
                )
                .header(HttpHeaders.CONTENT_TYPE, file.contentType() == null || file.contentType().isBlank() ? fallbackContentType : file.contentType())
                .body(file.content());
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

    private Locale resolveRequestedLocale(String lang, Locale fallbackLocale) {
        if (lang == null || lang.isBlank()) {
            return fallbackLocale == null ? Locale.ENGLISH : fallbackLocale;
        }

        String normalized = lang.trim().toLowerCase(Locale.ROOT);
        return switch (normalized) {
            case "ko", "ko-kr" -> Locale.KOREAN;
            case "de", "de-de" -> Locale.GERMAN;
            default -> Locale.ENGLISH;
        };
    }
}
