package com.sinwoo.business.leave.dto;

import java.math.BigDecimal;
import java.util.List;

public record LeaveReqResponse(
        String id,
        Integer no,
        String leaveType,
        String deductionType,
        String leaveUnit,
        String startDate,
        String endDate,
        BigDecimal days,
        String approverStatus,
        String status,
        String createdAt,
        String attachmentName,
        String reason,
        List<LeaveAprvStepResponse> approvalSteps,
        List<LeavePartResponse> ccs,
        Boolean canEdit,
        Boolean canCancel,
        Boolean canApprove,
        Boolean canReject,
        String myRoleCd
) {
}
