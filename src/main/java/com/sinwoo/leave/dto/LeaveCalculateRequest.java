package com.sinwoo.leave.dto;

public record LeaveCalculateRequest(
        String leaveId,
        String leaveType,
        String deductionType,
        String leaveUnit,
        String startDate,
        String endDate
) {
}
