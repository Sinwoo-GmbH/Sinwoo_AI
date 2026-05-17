package com.sinwoo.platform.hol.controller;

import com.sinwoo.common.exception.ApiException;
import com.sinwoo.common.security.AuthenticatedUsr;
import com.sinwoo.platform.hol.dto.RgnHolListResponse;
import com.sinwoo.platform.hol.service.RgnHolService;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/region-holidays")
@RequiredArgsConstructor
public class RgnHolController {

    private final RgnHolService rgnHolService;

    /** 연도별 내 지역 공휴일 조회 */
    @GetMapping
    public RgnHolListResponse getMyRgnHols(
            Authentication authentication,
            @RequestParam Short yr
    ) {
        return rgnHolService.getMyRgnHols(requireAuthenticatedUsr(authentication), yr);
    }

    /** 기간별 내 지역 공휴일 조회 (캘린더 뷰용) */
    @GetMapping("/period")
    public RgnHolListResponse getMyRgnHolsByPeriod(
            Authentication authentication,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return rgnHolService.getMyRgnHolsByPeriod(requireAuthenticatedUsr(authentication), from, to);
    }

    /** 공휴일 동기화 트리거 (관리자) */
    @PostMapping("/sync")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void syncRgnHols(@RequestParam Short yr) {
        rgnHolService.syncRgnHols(yr);
    }

    // ── helper ──────────────────────────────────────────────

    private AuthenticatedUsr requireAuthenticatedUsr(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedUsr usr)) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "AUTH_AUTHENTICATION_REQUIRED", "Authentication is required.");
        }
        return usr;
    }
}
