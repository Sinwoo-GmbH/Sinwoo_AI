package com.sinwoo.leave.dto;

import java.util.List;

public record LeaveOrganizationResponse(
        String id,
        String label,
        List<LeaveOrganizationResponse> children
) {
}
