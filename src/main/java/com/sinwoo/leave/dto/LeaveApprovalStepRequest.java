package com.sinwoo.leave.dto;

import java.util.List;

public record LeaveApprovalStepRequest(
        Integer order,
        List<String> userIds
) {
}
