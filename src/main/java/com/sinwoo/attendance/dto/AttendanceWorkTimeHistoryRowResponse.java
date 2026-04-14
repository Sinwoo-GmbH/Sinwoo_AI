package com.sinwoo.attendance.dto;

public record AttendanceWorkTimeHistoryRowResponse(
        Long attndId,
        String attndDt,
        Long usrId,
        Long empId,
        String empNo,
        String empNm,
        Long deptId,
        String deptNm,
        String attndStsCd,
        String attndStsNm,
        String chkinTm,
        String chkoutTm,
        Integer workMinuteCnt
) {
}
