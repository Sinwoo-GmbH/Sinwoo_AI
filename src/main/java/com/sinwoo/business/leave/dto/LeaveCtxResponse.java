package com.sinwoo.business.leave.dto;

import java.util.List;

public record LeaveCtxResponse(
        LeaveApplResponse applicant,
        LeaveBalResponse balance,
        List<String> leaveTypeOpts,
        List<String> deductionTypeOpts,
        List<String> leaveUnitOpts,
        List<String> statusOpts,
        List<LeaveOrgResponse> organizations,
        List<LeavePartResponse> emps
) {
}
