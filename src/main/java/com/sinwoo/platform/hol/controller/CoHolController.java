package com.sinwoo.platform.hol.controller;

import com.sinwoo.common.exception.ApiException;
import com.sinwoo.common.security.AuthenticatedUsr;
import com.sinwoo.platform.hol.dto.CoHolListResponse;
import com.sinwoo.platform.hol.dto.CoHolResponse;
import com.sinwoo.platform.hol.dto.CreateCoHolRequest;
import com.sinwoo.platform.hol.dto.UpdateCoHolRequest;
import com.sinwoo.platform.hol.service.CoHolService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/company-holidays")
@RequiredArgsConstructor
public class CoHolController {

    private final CoHolService coHolService;

    /** 회사 휴일 전체 목록 */
    @GetMapping
    public CoHolListResponse getCoHols(Authentication authentication) {
        return coHolService.getCoHols(requireAuthenticatedUsr(authentication));
    }

    /** 기간별 회사 휴일 조회 (캘린더 뷰용) */
    @GetMapping("/period")
    public CoHolListResponse getCoHolsByPeriod(
            Authentication authentication,
            @RequestParam Short yr,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return coHolService.getCoHolsByPeriod(requireAuthenticatedUsr(authentication), yr, from, to);
    }

    /** 회사 휴일 등록 (customer admin) */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CoHolResponse createCoHol(
            Authentication authentication,
            @Valid @RequestBody CreateCoHolRequest request
    ) {
        return coHolService.createCoHol(requireAuthenticatedUsr(authentication), request);
    }

    /** 회사 휴일 수정 (customer admin) */
    @PutMapping("/{coHolId}")
    public CoHolResponse updateCoHol(
            Authentication authentication,
            @PathVariable Long coHolId,
            @Valid @RequestBody UpdateCoHolRequest request
    ) {
        return coHolService.updateCoHol(requireAuthenticatedUsr(authentication), coHolId, request);
    }

    /** 회사 휴일 삭제 (customer admin, soft delete) */
    @DeleteMapping("/{coHolId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCoHol(
            Authentication authentication,
            @PathVariable Long coHolId
    ) {
        coHolService.deleteCoHol(requireAuthenticatedUsr(authentication), coHolId);
    }

    // ── helper ──────────────────────────────────────────────

    private AuthenticatedUsr requireAuthenticatedUsr(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedUsr usr)) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "AUTH_AUTHENTICATION_REQUIRED", "Authentication is required.");
        }
        return usr;
    }
}
