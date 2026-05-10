package com.sinwoo.business.leave.dto;

public record LeaveCalcRequest(
        String leaveId,
        String leaveType,
        String deductionType,
        String leaveUnit,
        String startDate,
        String endDate
) {
}
