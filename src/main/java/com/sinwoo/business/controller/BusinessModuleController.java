package com.sinwoo.business.controller;

import com.sinwoo.auth.support.AuthErrorCode;
import com.sinwoo.business.dto.BusinessModuleListResponse;
import com.sinwoo.business.dto.BusinessModuleResponse;
import com.sinwoo.business.dto.BusinessRecordListResponse;
import com.sinwoo.business.dto.BusinessRecordQuery;
import com.sinwoo.business.dto.BusinessRecordResponse;
import com.sinwoo.business.dto.BusinessRecordSaveRequest;
import com.sinwoo.business.dto.BusinessRecordStatusRequest;
import com.sinwoo.business.dto.BusinessRelatedListResponse;
import com.sinwoo.business.service.BusinessModuleService;
import com.sinwoo.business.service.BusinessRecordService;
import com.sinwoo.common.security.AuthenticatedUser;
import com.sinwoo.common.web.ApiException;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/business/modules")
@RequiredArgsConstructor
public class BusinessModuleController {

    private final BusinessModuleService businessModuleService;
    private final BusinessRecordService businessRecordService;

    @GetMapping
    public BusinessModuleListResponse getBusinessModules(
            Authentication authentication,
            @RequestParam(required = false) String lang,
            Locale locale
    ) {
        return businessModuleService.getBusinessModules(
                requireAuthenticatedUser(authentication),
                resolveRequestedLocale(lang, locale)
        );
    }

    @GetMapping("/{moduleCd}")
    public BusinessModuleResponse getBusinessModule(
            Authentication authentication,
            @PathVariable String moduleCd,
            @RequestParam(required = false) String lang,
            Locale locale
    ) {
        return businessModuleService.getBusinessModule(
                requireAuthenticatedUser(authentication),
                moduleCd,
                resolveRequestedLocale(lang, locale)
        );
    }

    @GetMapping("/{moduleCd}/records")
    public BusinessRecordListResponse getBusinessRecords(
            Authentication authentication,
            @PathVariable String moduleCd,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String yearMonth,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "30") int size,
            @RequestParam(required = false) String lang,
            Locale locale
    ) {
        return businessRecordService.getRecords(
                requireAuthenticatedUser(authentication),
                moduleCd,
                new BusinessRecordQuery(keyword, yearMonth, page, size),
                resolveRequestedLocale(lang, locale)
        );
    }

    @PostMapping("/{moduleCd}/records")
    public BusinessRecordResponse createBusinessRecord(
            Authentication authentication,
            @PathVariable String moduleCd,
            @RequestBody(required = false) BusinessRecordSaveRequest request
    ) {
        return businessRecordService.createRecord(requireAuthenticatedUser(authentication), moduleCd, request);
    }

    @GetMapping("/{moduleCd}/records/{recordId}/related")
    public BusinessRelatedListResponse getBusinessRecordRelatedTables(
            Authentication authentication,
            @PathVariable String moduleCd,
            @PathVariable Long recordId
    ) {
        return businessRecordService.getRelatedRecords(requireAuthenticatedUser(authentication), moduleCd, recordId);
    }

    @PutMapping("/{moduleCd}/records/{recordId}")
    public BusinessRecordResponse updateBusinessRecord(
            Authentication authentication,
            @PathVariable String moduleCd,
            @PathVariable Long recordId,
            @RequestBody BusinessRecordSaveRequest request
    ) {
        return businessRecordService.updateRecord(requireAuthenticatedUser(authentication), moduleCd, recordId, request);
    }

    @PatchMapping("/{moduleCd}/records/{recordId}/status")
    public BusinessRecordResponse updateBusinessRecordStatus(
            Authentication authentication,
            @PathVariable String moduleCd,
            @PathVariable Long recordId,
            @RequestBody BusinessRecordStatusRequest request
    ) {
        return businessRecordService.updateRecordStatus(requireAuthenticatedUser(authentication), moduleCd, recordId, request);
    }

    @DeleteMapping("/{moduleCd}/records/{recordId}")
    public void deleteBusinessRecord(
            Authentication authentication,
            @PathVariable String moduleCd,
            @PathVariable Long recordId
    ) {
        businessRecordService.deleteRecord(requireAuthenticatedUser(authentication), moduleCd, recordId);
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
            Locale contextLocale = LocaleContextHolder.getLocale();
            if (contextLocale != null) {
                return contextLocale;
            }
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
