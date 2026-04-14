package com.sinwoo.menu.controller;

import com.sinwoo.auth.support.AuthErrorCode;
import com.sinwoo.common.security.AuthenticatedUser;
import com.sinwoo.common.web.ApiException;
import com.sinwoo.menu.dto.CreateMenuRequest;
import com.sinwoo.menu.dto.MenuListResponse;
import com.sinwoo.menu.dto.MenuResponse;
import com.sinwoo.menu.dto.MenuTreeResponse;
import com.sinwoo.menu.service.MenuService;
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
public class MenuController {

    private final MenuService menuService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MenuResponse createMenu(@Valid @RequestBody CreateMenuRequest request) {
        return menuService.createMenu(request);
    }

    @GetMapping
    public MenuListResponse getMenus(
            @RequestParam(required = false) String mnuScopeCd,
            @RequestParam(required = false) String lang
    ) {
        return withRequestedLocale(lang, () -> menuService.getMenus(mnuScopeCd));
    }

    @GetMapping("/visible")
    public MenuTreeResponse getVisibleMenus(
            @RequestParam List<String> roleCd,
            @RequestParam(required = false) String mnuScopeCd,
            @RequestParam(required = false) String lang
    ) {
        return withRequestedLocale(lang, () -> menuService.getVisibleMenus(roleCd, mnuScopeCd));
    }

    @GetMapping("/visible-by-user")
    public MenuTreeResponse getVisibleMenusByUsr(
            @RequestParam Long usrId,
            @RequestParam(required = false) String mnuScopeCd,
            @RequestParam(required = false) String lang
    ) {
        return withRequestedLocale(lang, () -> menuService.getVisibleMenusByUsr(usrId, mnuScopeCd));
    }

    @GetMapping("/my")
    public MenuTreeResponse getVisibleMenusForCurrentUser(
            Authentication authentication,
            @RequestParam(required = false) String mnuScopeCd,
            @RequestParam(required = false) String lang
    ) {
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedUser authenticatedUser)) {
            throw new ApiException(
                    AuthErrorCode.AUTH_AUTHENTICATION_REQUIRED.status(),
                    AuthErrorCode.AUTH_AUTHENTICATION_REQUIRED.code(),
                    AuthErrorCode.AUTH_AUTHENTICATION_REQUIRED.message()
            );
        }
        return withRequestedLocale(lang, () -> menuService.getVisibleMenusForCurrentUser(authenticatedUser, mnuScopeCd));
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
