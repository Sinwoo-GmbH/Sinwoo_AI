package com.sinwoo.leave.dto;

import java.util.List;

public record LeaveContextResponse(
        LeaveApplicantResponse applicant,
        LeaveBalanceResponse balance,
        List<String> leaveTypeOptions,
        List<String> deductionTypeOptions,
        List<String> leaveUnitOptions,
        List<String> statusOptions,
        List<LeaveOrganizationResponse> organizations,
        List<LeaveParticipantResponse> employees
) {
}
