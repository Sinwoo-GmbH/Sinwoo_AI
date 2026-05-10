package com.sinwoo.business.leave.dto;

import java.util.List;

public record LeaveAprvStepResponse(
        String id,
        Integer order,
        List<LeavePartResponse> usrs
) {
}
