package com.sinwoo.leave.dto;

import java.util.List;

public record LeaveListResponse(
        LeaveBalanceResponse balance,
        List<LeaveRequestResponse> itemList
) {
}
