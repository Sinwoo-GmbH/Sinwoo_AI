package com.sinwoo.business.attendance.dto;

import java.util.List;

public record AttndWorkTimeHistListResponse(
        String yearMonth,
        boolean ownOnlyYn,
        int totCnt,
        List<AttndWorkTimeHistRowResponse> itemList
) {
}
