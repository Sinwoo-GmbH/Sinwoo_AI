package com.sinwoo.platform.leave.controller;

import com.sinwoo.common.security.AuthenticatedUsr;
import com.sinwoo.platform.leave.dto.LeaveRequest;
import com.sinwoo.platform.leave.dto.LeaveResponse;
import com.sinwoo.platform.leave.service.LeaveReqService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/leaves")
@RequiredArgsConstructor
public class LeaveReqController {

    private final LeaveReqService leaveReqService;

    @GetMapping("/context")
    public LeaveResponse.Context getContext(Authentication auth) {
        return leaveReqService.getContext(usr(auth));
    }

    @GetMapping
    public LeaveResponse.ItemList getMyLeaves(
            Authentication auth,
            @RequestParam(required = false) String startDateFrom,
            @RequestParam(required = false) String startDateTo,
            @RequestParam(required = false) String status
    ) {
        return leaveReqService.getMyLeaves(usr(auth), startDateFrom, startDateTo, status);
    }

    @GetMapping("/{leaveId}")
    public LeaveResponse.Item getLeave(Authentication auth, @PathVariable Long leaveId) {
        return leaveReqService.getLeave(usr(auth), leaveId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public LeaveResponse.Item createLeave(Authentication auth, @Valid @RequestBody LeaveRequest request) {
        return leaveReqService.createLeave(usr(auth), request);
    }

    @PutMapping("/{leaveId}")
    public LeaveResponse.Item updateLeave(
            Authentication auth,
            @PathVariable Long leaveId,
            @Valid @RequestBody LeaveRequest request
    ) {
        return leaveReqService.updateLeave(usr(auth), leaveId, request);
    }

    @DeleteMapping("/{leaveId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteLeave(Authentication auth, @PathVariable Long leaveId) {
        leaveReqService.deleteLeave(usr(auth), leaveId);
    }

    @PostMapping("/{leaveId}/cancel")
    public LeaveResponse.Item cancelLeave(Authentication auth, @PathVariable Long leaveId) {
        return leaveReqService.cancelLeave(usr(auth), leaveId);
    }

    @PostMapping("/{leaveId}/confirm")
    public LeaveResponse.Item confirmLeave(Authentication auth, @PathVariable Long leaveId) {
        return leaveReqService.confirmLeave(usr(auth), leaveId);
    }

    @PostMapping("/{leaveId}/reject")
    public LeaveResponse.Item rejectLeave(
            Authentication auth,
            @PathVariable Long leaveId,
            @Valid @RequestBody LeaveRequest.Reject request
    ) {
        return leaveReqService.rejectLeave(usr(auth), leaveId, request.rejectReason());
    }

    @PostMapping("/calculate")
    public LeaveResponse.CalcResult calculateDays(Authentication auth, @Valid @RequestBody LeaveRequest.Calc request) {
        return leaveReqService.calculateDays(usr(auth), request);
    }

    private AuthenticatedUsr usr(Authentication auth) {
        if (auth == null || !(auth.getPrincipal() instanceof AuthenticatedUsr u)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
        return u;
    }
}
