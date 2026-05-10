package com.sinwoo.leave.dto;

import java.util.List;

public record LeaveApprovalStepResponse(
        String id,
        Integer order,
        List<LeaveParticipantResponse> users
) {
}
