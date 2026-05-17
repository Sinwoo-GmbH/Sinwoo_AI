package com.sinwoo.platform.leave.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * 휴가 도메인 요청 DTO — 모든 요청 구조를 하나의 파일에 통합.
 */
public record LeaveRequest(
        @NotBlank String leaveType,
        @NotBlank String deductionType,
        @NotBlank String leaveUnit,
        @NotBlank String startDate,
        @NotBlank String endDate,
        String attachmentName,
        String reason,
        @NotNull @Valid List<AprvStep> approvalSteps,
        @NotNull List<String> ccIds,
        @NotBlank String nextStatus
) {

    /** 결재 단계 */
    public record AprvStep(
            @NotNull Integer order,
            @NotNull List<String> usrIds
    ) {}

    /** 일수 계산 요청 */
    public record Calc(
            String leaveId,
            @NotBlank String leaveType,
            @NotBlank String deductionType,
            @NotBlank String leaveUnit,
            @NotBlank String startDate,
            @NotBlank String endDate
    ) {}

    /** 반려 요청 */
    public record Reject(
            @NotBlank String rejectReason
    ) {}
}
