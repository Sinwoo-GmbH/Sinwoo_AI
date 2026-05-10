package com.sinwoo.business.attendance.dto;

public record AttndWidgetPolicyResponse(
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
