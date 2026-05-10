package com.sinwoo.business.leave.service;

import com.sinwoo.common.security.AuthenticatedUsr;
import com.sinwoo.business.leave.dto.LeaveActRequest;
import com.sinwoo.business.leave.dto.LeaveCalcRequest;
import com.sinwoo.business.leave.dto.LeaveCalcResponse;
import com.sinwoo.business.leave.dto.LeaveCtxResponse;
import com.sinwoo.business.leave.dto.LeaveListResponse;
import com.sinwoo.business.leave.dto.LeaveReqResponse;
import com.sinwoo.business.leave.dto.LeaveSaveRequest;

public interface LeaveReqService {

    LeaveCtxResponse getContext(AuthenticatedUsr authenticatedUsr);

    LeaveListResponse getLeaves(
            AuthenticatedUsr authenticatedUsr,
            String startDateFrom,
            String startDateTo,
            String status
    );

    LeaveCalcResponse calculate(AuthenticatedUsr authenticatedUsr, LeaveCalcRequest request);

    LeaveReqResponse createLeave(AuthenticatedUsr authenticatedUsr, LeaveSaveRequest request);

    LeaveReqResponse updateLeave(AuthenticatedUsr authenticatedUsr, Long leaveId, LeaveSaveRequest request);

    LeaveReqResponse cancelLeave(AuthenticatedUsr authenticatedUsr, Long leaveId);

    LeaveReqResponse confirmLeave(AuthenticatedUsr authenticatedUsr, Long leaveId);

    LeaveReqResponse rejectLeave(AuthenticatedUsr authenticatedUsr, Long leaveId, LeaveActRequest request);
}
