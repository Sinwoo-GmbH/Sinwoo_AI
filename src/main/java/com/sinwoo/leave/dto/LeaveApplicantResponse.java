package com.sinwoo.leave.dto;

public record LeaveApplicantResponse(
        String name,
        String department,
        String position
) {
}
