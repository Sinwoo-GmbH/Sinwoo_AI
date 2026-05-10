package com.sinwoo.leave.service;

import com.sinwoo.common.security.AuthenticatedUser;
import com.sinwoo.leave.dto.LeaveActionRequest;
import com.sinwoo.leave.dto.LeaveCalculateRequest;
import com.sinwoo.leave.dto.LeaveCalculateResponse;
import com.sinwoo.leave.dto.LeaveContextResponse;
import com.sinwoo.leave.dto.LeaveListResponse;
import com.sinwoo.leave.dto.LeaveRequestResponse;
import com.sinwoo.leave.dto.LeaveSaveRequest;

public interface LeaveRequestService {

    LeaveContextResponse getContext(AuthenticatedUser authenticatedUser);

    LeaveListResponse getLeaves(
            AuthenticatedUser authenticatedUser,
            String startDateFrom,
            String startDateTo,
            String status
    );

    LeaveCalculateResponse calculate(AuthenticatedUser authenticatedUser, LeaveCalculateRequest request);

    LeaveRequestResponse createLeave(AuthenticatedUser authenticatedUser, LeaveSaveRequest request);

    LeaveRequestResponse updateLeave(AuthenticatedUser authenticatedUser, Long leaveId, LeaveSaveRequest request);

    LeaveRequestResponse cancelLeave(AuthenticatedUser authenticatedUser, Long leaveId);

    LeaveRequestResponse confirmLeave(AuthenticatedUser authenticatedUser, Long leaveId);

    LeaveRequestResponse rejectLeave(AuthenticatedUser authenticatedUser, Long leaveId, LeaveActionRequest request);
}
