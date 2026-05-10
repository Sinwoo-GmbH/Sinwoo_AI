package com.sinwoo.business.leave.dto;

public record LeaveDupResponse(
        String type,
        String id,
        String startDate,
        String endDate
) {
}
