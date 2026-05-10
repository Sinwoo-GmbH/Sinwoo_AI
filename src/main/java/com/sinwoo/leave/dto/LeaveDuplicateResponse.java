package com.sinwoo.leave.dto;

public record LeaveDuplicateResponse(
        String type,
        String id,
        String startDate,
        String endDate
) {
}
