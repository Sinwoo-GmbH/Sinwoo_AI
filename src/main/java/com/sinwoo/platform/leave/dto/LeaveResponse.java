package com.sinwoo.platform.leave.dto;

import com.sinwoo.platform.leave.domain.LeaveReq;
import java.util.List;

/**
 * 휴가 도메인 응답 DTO — 모든 응답 구조를 하나의 파일에 통합.
 */
public record LeaveResponse() {

    /** 휴가 신청 단건 */
    public record Item(
            String id,
            int no,
            String leaveType,
            String deductionType,
            String leaveUnit,
            String startDate,
            String endDate,
            double days,
            String approverStatus,
            String status,
            String createdAt,
            String attachmentName,
            String reason,
            List<AprvStep> approvalSteps,
            List<Part> ccs,
            Boolean canEdit,
            Boolean canCancel,
            Boolean canDelete,
            Boolean canApprove,
            Boolean canReject,
            String myRoleCd
    ) {
        public static Item from(
                LeaveReq req, int no,
                String approverStatus,
                List<AprvStep> approvalSteps,
                List<Part> ccs,
                Boolean canEdit, Boolean canCancel, Boolean canDelete,
                Boolean canApprove, Boolean canReject,
                String myRoleCd
        ) {
            return new Item(
                    String.valueOf(req.getId()),
                    no,
                    req.getLeaveTpCd(),
                    req.getDeductTpCd(),
                    req.getLeaveUnitCd(),
                    req.getStrDt().toString(),
                    req.getEndDt().toString(),
                    req.getUseDays() != null ? req.getUseDays().doubleValue() : 0,
                    approverStatus,
                    req.getStsCd(),
                    req.getCreatedAt() != null ? req.getCreatedAt().toString() : "",
                    req.getAtchFileNm(),
                    req.getReason(),
                    approvalSteps,
                    ccs,
                    canEdit, canCancel, canDelete, canApprove, canReject, myRoleCd
            );
        }
    }

    /** 목록 응답 (잔여일수 + 신청 목록) */
    public record ItemList(
            Balance balance,
            List<Item> itemList
    ) {}

    /** 잔여일수 */
    public record Balance(
            double availableDays,
            double afterRequestDays,
            double previousYearDays,
            double currentYearDays
    ) {}

    /** 신청자 프로필 */
    public record Applicant(
            String name,
            String dept,
            String position
    ) {}

    /** 결재자/참조자 */
    public record Part(
            String id,
            String name,
            String dept,
            String position,
            String orgId
    ) {}

    /** 조직 트리 노드 */
    public record Org(
            String id,
            String label,
            List<Org> children
    ) {}

    /** 결재 단계 */
    public record AprvStep(
            String id,
            int order,
            List<Part> usrs
    ) {}

    /** 컨텍스트 (신청 화면 초기화용) */
    public record Context(
            Applicant applicant,
            Balance balance,
            List<String> leaveTypeOpts,
            List<String> deductionTypeOpts,
            List<String> leaveUnitOpts,
            List<String> statusOpts,
            List<Org> organizations,
            List<Part> emps
    ) {}

    /** 일수 계산 결과 */
    public record CalcResult(
            String resultCd,
            String resultMsg,
            double previousYearDays,
            double currentYearDays,
            double days,
            double afterRequestDays,
            List<Dup> duplicates
    ) {}

    /** 중복 휴가 */
    public record Dup(
            String type,
            String id,
            String startDate,
            String endDate
    ) {}
}
