package com.sinwoo.business.attendance.dto;

import java.util.List;

public record AttndWorkTimeFiltOptsResponse(
        boolean ownOnlyYn,
        int empTotCnt,
        List<AttndWorkTimeFiltOptResponse> empList,
        int deptTotCnt,
        List<AttndWorkTimeFiltOptResponse> deptList
) {
}
