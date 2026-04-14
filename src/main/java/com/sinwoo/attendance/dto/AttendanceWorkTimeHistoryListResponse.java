package com.sinwoo.attendance.dto;

import java.util.List;

public record AttendanceWorkTimeHistoryListResponse(
        String yearMonth,
        boolean ownOnlyYn,
        int totCnt,
        List<AttendanceWorkTimeHistoryRowResponse> itemList
) {
}
