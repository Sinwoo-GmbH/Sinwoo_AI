package com.sinwoo.business.leave.dto;

public record LeavePartResponse(
        String id,
        String name,
        String dept,
        String position,
        String orgId
) {
}
