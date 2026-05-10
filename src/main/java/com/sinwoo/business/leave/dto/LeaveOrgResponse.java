package com.sinwoo.business.leave.dto;

import java.util.List;

public record LeaveOrgResponse(
        String id,
        String label,
        List<LeaveOrgResponse> children
) {
}
