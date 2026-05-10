package com.sinwoo.business.module.controller;

import com.sinwoo.platform.auth.support.AuthErrorCd;
import com.sinwoo.business.module.dto.BizModListResponse;
import com.sinwoo.business.module.dto.BizModResponse;
import com.sinwoo.business.module.dto.BizRecListResponse;
import com.sinwoo.business.module.dto.BizRecQuery;
import com.sinwoo.business.module.dto.BizRecResponse;
import com.sinwoo.business.module.dto.BizRecSaveRequest;
import com.sinwoo.business.module.dto.BizRecStatusRequest;
import com.sinwoo.business.module.dto.BizRelListResponse;
import com.sinwoo.business.module.service.BizModService;
import com.sinwoo.business.module.service.BizRecService;
import com.sinwoo.common.security.AuthenticatedUsr;
import com.sinwoo.common.exception.ApiException;
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
public class BizModController {

    private final BizModService bizModService;
    private final BizRecService bizRecService;

    @GetMapping
    public BizModListResponse getBizMods(
            Authentication authentication,
            @RequestParam(required = false) String lang,
            Locale locale
    ) {
        return bizModService.getBizMods(
                requireAuthenticatedUsr(authentication),
                resolveRequestedLocale(lang, locale)
        );
    }

    @GetMapping("/{modCd}")
    public BizModResponse getBizMod(
            Authentication authentication,
            @PathVariable String modCd,
            @RequestParam(required = false) String lang,
            Locale locale
    ) {
        return bizModService.getBizMod(
                requireAuthenticatedUsr(authentication),
                modCd,
                resolveRequestedLocale(lang, locale)
        );
    }

    @GetMapping("/{modCd}/records")
    public BizRecListResponse getBizRecs(
            Authentication authentication,
            @PathVariable String modCd,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String yearMonth,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "30") int size,
            @RequestParam(required = false) String lang,
            Locale locale
    ) {
        return bizRecService.getRecs(
                requireAuthenticatedUsr(authentication),
                modCd,
                new BizRecQuery(keyword, yearMonth, page, size),
                resolveRequestedLocale(lang, locale)
        );
    }

    @PostMapping("/{modCd}/records")
    public BizRecResponse createBizRec(
            Authentication authentication,
            @PathVariable String modCd,
            @RequestBody(required = false) BizRecSaveRequest request
    ) {
        return bizRecService.createRec(requireAuthenticatedUsr(authentication), modCd, request);
    }

    @GetMapping("/{modCd}/records/{recId}/related")
    public BizRelListResponse getBizRecRelTbls(
            Authentication authentication,
            @PathVariable String modCd,
            @PathVariable Long recId
    ) {
        return bizRecService.getRelRecs(requireAuthenticatedUsr(authentication), modCd, recId);
    }

    @PutMapping("/{modCd}/records/{recId}")
    public BizRecResponse updateBizRec(
            Authentication authentication,
            @PathVariable String modCd,
            @PathVariable Long recId,
            @RequestBody BizRecSaveRequest request
    ) {
        return bizRecService.updateRec(requireAuthenticatedUsr(authentication), modCd, recId, request);
    }

    @PatchMapping("/{modCd}/records/{recId}/status")
    public BizRecResponse updateBizRecStatus(
            Authentication authentication,
            @PathVariable String modCd,
            @PathVariable Long recId,
            @RequestBody BizRecStatusRequest request
    ) {
        return bizRecService.updateRecStatus(requireAuthenticatedUsr(authentication), modCd, recId, request);
    }

    @DeleteMapping("/{modCd}/records/{recId}")
    public void deleteBizRec(
            Authentication authentication,
            @PathVariable String modCd,
            @PathVariable Long recId
    ) {
        bizRecService.deleteRec(requireAuthenticatedUsr(authentication), modCd, recId);
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
