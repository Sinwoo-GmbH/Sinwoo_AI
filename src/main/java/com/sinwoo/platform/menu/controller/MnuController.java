package com.sinwoo.platform.mnu.controller;

import com.sinwoo.platform.auth.support.AuthErrorCd;
import com.sinwoo.common.security.AuthenticatedUsr;
import com.sinwoo.common.exception.ApiException;
import com.sinwoo.platform.mnu.dto.CreateMnuRequest;
import com.sinwoo.platform.mnu.dto.MnuListResponse;
import com.sinwoo.platform.mnu.dto.MnuResponse;
import com.sinwoo.platform.mnu.dto.MnuTreeResponse;
import com.sinwoo.platform.mnu.service.MnuService;
import jakarta.validation.Valid;
import java.util.Locale;
import java.util.List;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/menus")
@RequiredArgsConstructor
public class MnuController {

    private final MnuService mnuService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MnuResponse createMnu(@Valid @RequestBody CreateMnuRequest request) {
        return mnuService.createMnu(request);
    }

    @GetMapping
    public MnuListResponse getMnus(
            @RequestParam(required = false) String mnuScopeCd,
            @RequestParam(required = false) String lang
    ) {
        return withRequestedLocale(lang, () -> mnuService.getMnus(mnuScopeCd));
    }

    @GetMapping("/visible")
    public MnuTreeResponse getVisibleMnus(
            @RequestParam List<String> roleCd,
            @RequestParam(required = false) String mnuScopeCd,
            @RequestParam(required = false) String lang
    ) {
        return withRequestedLocale(lang, () -> mnuService.getVisibleMnus(roleCd, mnuScopeCd));
    }

    @GetMapping("/visible-by-user")
    public MnuTreeResponse getVisibleMnusByUsr(
            @RequestParam Long usrId,
            @RequestParam(required = false) String mnuScopeCd,
            @RequestParam(required = false) String lang
    ) {
        return withRequestedLocale(lang, () -> mnuService.getVisibleMnusByUsr(usrId, mnuScopeCd));
    }

    @GetMapping("/visible-by-login")
    public MnuTreeResponse getVisibleMnusByLgnId(
            @RequestParam String tenantCd,
            @RequestParam String lgnId,
            @RequestParam(required = false) String mnuScopeCd,
            @RequestParam(required = false) String lang
    ) {
        return withRequestedLocale(lang, () -> mnuService.getVisibleMnusByLgnId(tenantCd, lgnId, mnuScopeCd));
    }

    @GetMapping("/my")
    public MnuTreeResponse getVisibleMnusForCurrentUsr(
            Authentication authentication,
            @RequestParam(required = false) String mnuScopeCd,
            @RequestParam(required = false) String lang
    ) {
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedUsr authenticatedUsr)) {
            throw new ApiException(
                    AuthErrorCd.AUTH_AUTHENTICATION_REQUIRED.status(),
                    AuthErrorCd.AUTH_AUTHENTICATION_REQUIRED.code(),
                    AuthErrorCd.AUTH_AUTHENTICATION_REQUIRED.message()
            );
        }
        return withRequestedLocale(lang, () -> mnuService.getVisibleMnusForCurrentUsr(authenticatedUsr, mnuScopeCd));
    }

    private <T> T withRequestedLocale(String lang, Supplier<T> action) {
        Locale previousLocale = LocaleContextHolder.getLocale();
        try {
            LocaleContextHolder.setLocale(resolveRequestedLocale(lang, previousLocale));
            return action.get();
        } finally {
            LocaleContextHolder.setLocale(previousLocale);
        }
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
