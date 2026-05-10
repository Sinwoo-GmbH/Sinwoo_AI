package com.sinwoo.business.leave.controller;

import com.sinwoo.platform.auth.support.AuthErrorCd;
import com.sinwoo.common.security.AuthenticatedUsr;
import com.sinwoo.common.exception.ApiException;
import com.sinwoo.business.leave.dto.LeaveActRequest;
import com.sinwoo.business.leave.dto.LeaveCalcRequest;
import com.sinwoo.business.leave.dto.LeaveCalcResponse;
import com.sinwoo.business.leave.dto.LeaveCtxResponse;
import com.sinwoo.business.leave.dto.LeaveListResponse;
import com.sinwoo.business.leave.dto.LeaveReqResponse;
import com.sinwoo.business.leave.dto.LeaveSaveRequest;
import com.sinwoo.business.leave.service.LeaveReqService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/leaves")
@RequiredArgsConstructor
public class LeaveReqController {

    private final LeaveReqService leaveReqService;

    @GetMapping("/context")
    public LeaveCtxResponse getContext(Authentication authentication) {
        return leaveReqService.getContext(requireAuthenticatedUsr(authentication));
    }

    @GetMapping
    public LeaveListResponse getLeaves(
            Authentication authentication,
            @RequestParam(required = false) String startDateFrom,
            @RequestParam(required = false) String startDateTo,
            @RequestParam(required = false) String status
    ) {
        return leaveReqService.getLeaves(
                requireAuthenticatedUsr(authentication),
                startDateFrom,
                startDateTo,
                status
        );
    }

    @PostMapping("/calculate")
    public LeaveCalcResponse calculate(
            Authentication authentication,
            @RequestBody LeaveCalcRequest request
    ) {
        return leaveReqService.calculate(requireAuthenticatedUsr(authentication), request);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public LeaveReqResponse createLeave(
            Authentication authentication,
            @RequestBody LeaveSaveRequest request
    ) {
        return leaveReqService.createLeave(requireAuthenticatedUsr(authentication), request);
    }

    @PutMapping("/{leaveId}")
    public LeaveReqResponse updateLeave(
            Authentication authentication,
            @PathVariable Long leaveId,
            @RequestBody LeaveSaveRequest request
    ) {
        return leaveReqService.updateLeave(requireAuthenticatedUsr(authentication), leaveId, request);
    }

    @PatchMapping("/{leaveId}/cancel")
    public LeaveReqResponse cancelLeave(
            Authentication authentication,
            @PathVariable Long leaveId
    ) {
        return leaveReqService.cancelLeave(requireAuthenticatedUsr(authentication), leaveId);
    }

    @PostMapping("/{leaveId}/confirm")
    public LeaveReqResponse confirmLeave(
            Authentication authentication,
            @PathVariable Long leaveId
    ) {
        return leaveReqService.confirmLeave(requireAuthenticatedUsr(authentication), leaveId);
    }

    @PostMapping("/{leaveId}/reject")
    public LeaveReqResponse rejectLeave(
            Authentication authentication,
            @PathVariable Long leaveId,
            @RequestBody(required = false) LeaveActRequest request
    ) {
        return leaveReqService.rejectLeave(requireAuthenticatedUsr(authentication), leaveId, request);
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
}
