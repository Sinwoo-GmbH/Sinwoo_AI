package com.sinwoo.leave.dto;

public record LeaveParticipantResponse(
        String id,
        String name,
        String department,
        String position,
        String orgId
) {
}
