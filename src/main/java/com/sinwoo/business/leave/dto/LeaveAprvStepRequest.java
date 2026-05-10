package com.sinwoo.business.leave.dto;

import java.util.List;

public record LeaveAprvStepRequest(
        Integer order,
        List<String> usrIds
) {
}
