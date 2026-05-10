package com.sinwoo.business.attendance.controller;

import com.sinwoo.business.attendance.dto.AttndRptExportFile;
import com.sinwoo.business.attendance.dto.AttndWorkTimeFiltOptsResponse;
import com.sinwoo.business.attendance.dto.AttndWorkTimeHistListResponse;
import com.sinwoo.business.attendance.dto.AttndWorkTimeHistQuery;
import com.sinwoo.business.attendance.service.AttndRptService;
import com.sinwoo.platform.auth.support.AuthErrorCd;
import com.sinwoo.common.security.AuthenticatedUsr;
import com.sinwoo.common.exception.ApiException;
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
public class AttndRptController {

    private static final String EXCEL_CONTENT_TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    private final AttndRptService attndRptService;

    @GetMapping("/history")
    public AttndWorkTimeHistListResponse getWorkTimeHist(
            Authentication authentication,
            @RequestParam(required = false) String yearMonth,
            @RequestParam(required = false) String empNm,
            @RequestParam(required = false) String deptNm,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String lang,
            Locale locale
    ) {
        return attndRptService.getWorkTimeHist(
                requireAuthenticatedUsr(authentication),
                new AttndWorkTimeHistQuery(yearMonth, empNm, deptNm, keyword),
                resolveRequestedLocale(lang, locale)
        );
    }

    @GetMapping("/filter-options")
    public AttndWorkTimeFiltOptsResponse getWorkTimeFiltOpts(Authentication authentication) {
        return attndRptService.getWorkTimeFiltOpts(requireAuthenticatedUsr(authentication));
    }

    @GetMapping("/history/export/excel")
    public ResponseEntity<byte[]> exportWorkTimeHistExcel(
            Authentication authentication,
            @RequestParam(required = false) String yearMonth,
            @RequestParam(required = false) String empNm,
            @RequestParam(required = false) String deptNm,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String lang,
            Locale locale
    ) {
        AttndRptExportFile file = attndRptService.exportWorkTimeHistExcel(
                requireAuthenticatedUsr(authentication),
                new AttndWorkTimeHistQuery(yearMonth, empNm, deptNm, keyword),
                resolveRequestedLocale(lang, locale)
        );
        return buildExportResponse(file, EXCEL_CONTENT_TYPE);
    }

    @GetMapping("/history/export/pdf")
    public ResponseEntity<byte[]> exportWorkTimeHistPdf(
            Authentication authentication,
            @RequestParam(required = false) String yearMonth,
            @RequestParam(required = false) String empNm,
            @RequestParam(required = false) String deptNm,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String lang,
            Locale locale
    ) {
        AttndRptExportFile file = attndRptService.exportWorkTimeHistPdf(
                requireAuthenticatedUsr(authentication),
                new AttndWorkTimeHistQuery(yearMonth, empNm, deptNm, keyword),
                resolveRequestedLocale(lang, locale)
        );
        return buildExportResponse(file, "application/pdf");
    }

    private ResponseEntity<byte[]> buildExportResponse(AttndRptExportFile file, String fallbackCntType) {
        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + file.fileNm().replace("\"", "") + "\"; filename*=UTF-8''"
                                + java.net.URLEncoder.encode(file.fileNm(), StandardCharsets.UTF_8).replace("+", "%20")
                )
                .header(HttpHeaders.CONTENT_TYPE, file.contentType() == null || file.contentType().isBlank() ? fallbackCntType : file.contentType())
                .body(file.cnt());
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
