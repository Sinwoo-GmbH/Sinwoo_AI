package com.sinwoo.attendance.dto;

public record AttendanceWidgetPolicyResponse(
        String bizTmznId,
        String dfltChkinTm,
        String dfltChkoutTm,
        String attndFlagGrpCd,
        String chkinStsCd,
        String chkoutStsCd,
        String leaveStsCd,
        String bizTripStsCd
) {
}
