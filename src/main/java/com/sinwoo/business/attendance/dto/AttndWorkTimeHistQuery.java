package com.sinwoo.business.attendance.dto;

public record AttndWorkTimeHistQuery(
        String yearMonth,
        String empNm,
        String deptNm,
        String keyword
) {
}
