package com.sinwoo.business.leave.dto;

import java.util.List;

public record LeaveListResponse(
        LeaveBalResponse balance,
        List<LeaveReqResponse> itemList
) {
}
