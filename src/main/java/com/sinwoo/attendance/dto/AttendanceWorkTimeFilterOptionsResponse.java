package com.sinwoo.attendance.dto;

import java.util.List;

public record AttendanceWorkTimeFilterOptionsResponse(
        boolean ownOnlyYn,
        int empTotCnt,
        List<AttendanceWorkTimeFilterOptionResponse> empList,
        int deptTotCnt,
        List<AttendanceWorkTimeFilterOptionResponse> deptList
) {
}
