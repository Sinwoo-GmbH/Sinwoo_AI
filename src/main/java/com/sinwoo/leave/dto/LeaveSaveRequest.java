package com.sinwoo.leave.dto;

import java.util.List;

public record LeaveSaveRequest(
        String leaveType,
        String deductionType,
        String leaveUnit,
        String startDate,
        String endDate,
        String attachmentName,
        String reason,
        List<LeaveApprovalStepRequest> approvalSteps,
        List<String> ccIds,
        String nextStatus
) {
}
