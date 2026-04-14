package com.sinwoo.attendance.dto;

public record AttendanceReportExportFile(
        String fileNm,
        String contentType,
        byte[] content
) {
}
