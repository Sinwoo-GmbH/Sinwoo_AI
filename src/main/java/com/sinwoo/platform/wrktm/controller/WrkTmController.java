package com.sinwoo.platform.wrktm.controller;

import com.sinwoo.common.exception.ApiException;
import com.sinwoo.common.security.AuthenticatedUsr;
import com.sinwoo.platform.wrktm.dto.ClockInRequest;
import com.sinwoo.platform.wrktm.dto.ClockOutRequest;
import com.sinwoo.platform.wrktm.dto.SaveWrkTmRequest;
import com.sinwoo.platform.wrktm.dto.WrkTmListResponse;
import com.sinwoo.platform.wrktm.dto.WrkTmResponse;
import com.sinwoo.platform.wrktm.service.WrkTmService;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/work-times")
@RequiredArgsConstructor
public class WrkTmController {

    private final WrkTmService wrkTmService;

    /** Dashboard: 출근 등록 */
    @PostMapping("/clock-in")
    @ResponseStatus(HttpStatus.CREATED)
    public WrkTmResponse clockIn(
            Authentication authentication,
            @Valid @RequestBody ClockInRequest request
    ) {
        return wrkTmService.clockIn(requireAuthenticatedUsr(authentication), request);
    }

    /** Dashboard: 퇴근 등록 */
    @PostMapping("/clock-out")
    public WrkTmResponse clockOut(
            Authentication authentication,
            @Valid @RequestBody ClockOutRequest request
    ) {
        return wrkTmService.clockOut(requireAuthenticatedUsr(authentication), request);
    }

    /** My Working Time: 출퇴근 저장 (생성/수정) */
    @PostMapping("/save")
    public WrkTmResponse saveWrkTm(
            Authentication authentication,
            @Valid @RequestBody SaveWrkTmRequest request
    ) {
        return wrkTmService.saveWrkTm(requireAuthenticatedUsr(authentication), request);
    }

    /** My Working Time: 출퇴근 삭제 (soft delete) */
    @DeleteMapping("/{wrkTmId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteWrkTm(
            Authentication authentication,
            @PathVariable Long wrkTmId
    ) {
        wrkTmService.deleteWrkTm(requireAuthenticatedUsr(authentication), wrkTmId);
    }

    /** My Working Time: 특정 날짜 조회 */
    @GetMapping("/date")
    public WrkTmResponse getWrkTm(
            Authentication authentication,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate workDt
    ) {
        return wrkTmService.getWrkTm(requireAuthenticatedUsr(authentication), workDt);
    }

    /** My Working Time: 기간 조회 (캘린더 월간 뷰) */
    @GetMapping("/my")
    public WrkTmListResponse getMyWrkTms(
            Authentication authentication,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return wrkTmService.getMyWrkTms(requireAuthenticatedUsr(authentication), from, to);
    }

    /** Report: 전직원 기간 조회 (관리자) */
    @GetMapping("/all")
    public WrkTmListResponse getAllWrkTms(
            Authentication authentication,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return wrkTmService.getAllWrkTms(requireAuthenticatedUsr(authentication), from, to);
    }

    // ── helper ──────────────────────────────────────────────

    private AuthenticatedUsr requireAuthenticatedUsr(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedUsr usr)) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "AUTH_AUTHENTICATION_REQUIRED", "Authentication is required.");
        }
        return usr;
    }
}
