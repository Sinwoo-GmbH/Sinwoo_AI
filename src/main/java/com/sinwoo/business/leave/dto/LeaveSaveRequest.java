package com.sinwoo.business.leave.dto;

import java.util.List;

public record LeaveSaveRequest(
        String leaveType,
        String deductionType,
        String leaveUnit,
        String startDate,
        String endDate,
        String attachmentName,
        String reason,
        List<LeaveAprvStepRequest> approvalSteps,
        List<String> ccIds,
        String nextStatus
) {
}
