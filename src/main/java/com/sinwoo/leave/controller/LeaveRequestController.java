package com.sinwoo.leave.controller;

import com.sinwoo.auth.support.AuthErrorCode;
import com.sinwoo.common.security.AuthenticatedUser;
import com.sinwoo.common.web.ApiException;
import com.sinwoo.leave.dto.LeaveActionRequest;
import com.sinwoo.leave.dto.LeaveCalculateRequest;
import com.sinwoo.leave.dto.LeaveCalculateResponse;
import com.sinwoo.leave.dto.LeaveContextResponse;
import com.sinwoo.leave.dto.LeaveListResponse;
import com.sinwoo.leave.dto.LeaveRequestResponse;
import com.sinwoo.leave.dto.LeaveSaveRequest;
import com.sinwoo.leave.service.LeaveRequestService;
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
public class LeaveRequestController {

    private final LeaveRequestService leaveRequestService;

    @GetMapping("/context")
    public LeaveContextResponse getContext(Authentication authentication) {
        return leaveRequestService.getContext(requireAuthenticatedUser(authentication));
    }

    @GetMapping
    public LeaveListResponse getLeaves(
            Authentication authentication,
            @RequestParam(required = false) String startDateFrom,
            @RequestParam(required = false) String startDateTo,
            @RequestParam(required = false) String status
    ) {
        return leaveRequestService.getLeaves(
                requireAuthenticatedUser(authentication),
                startDateFrom,
                startDateTo,
                status
        );
    }

    @PostMapping("/calculate")
    public LeaveCalculateResponse calculate(
            Authentication authentication,
            @RequestBody LeaveCalculateRequest request
    ) {
        return leaveRequestService.calculate(requireAuthenticatedUser(authentication), request);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public LeaveRequestResponse createLeave(
            Authentication authentication,
            @RequestBody LeaveSaveRequest request
    ) {
        return leaveRequestService.createLeave(requireAuthenticatedUser(authentication), request);
    }

    @PutMapping("/{leaveId}")
    public LeaveRequestResponse updateLeave(
            Authentication authentication,
            @PathVariable Long leaveId,
            @RequestBody LeaveSaveRequest request
    ) {
        return leaveRequestService.updateLeave(requireAuthenticatedUser(authentication), leaveId, request);
    }

    @PatchMapping("/{leaveId}/cancel")
    public LeaveRequestResponse cancelLeave(
            Authentication authentication,
            @PathVariable Long leaveId
    ) {
        return leaveRequestService.cancelLeave(requireAuthenticatedUser(authentication), leaveId);
    }

    @PostMapping("/{leaveId}/confirm")
    public LeaveRequestResponse confirmLeave(
            Authentication authentication,
            @PathVariable Long leaveId
    ) {
        return leaveRequestService.confirmLeave(requireAuthenticatedUser(authentication), leaveId);
    }

    @PostMapping("/{leaveId}/reject")
    public LeaveRequestResponse rejectLeave(
            Authentication authentication,
            @PathVariable Long leaveId,
            @RequestBody(required = false) LeaveActionRequest request
    ) {
        return leaveRequestService.rejectLeave(requireAuthenticatedUser(authentication), leaveId, request);
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
}
