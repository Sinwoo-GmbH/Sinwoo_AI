package com.sinwoo.business.attendance.dto;

public record AttndRptExportFile(
        String fileNm,
        String contentType,
        byte[] cnt
) {
}
