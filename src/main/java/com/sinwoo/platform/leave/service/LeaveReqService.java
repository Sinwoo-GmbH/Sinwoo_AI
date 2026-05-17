package com.sinwoo.platform.leave.service;

import com.sinwoo.common.security.AuthenticatedUsr;
import com.sinwoo.platform.leave.dto.LeaveRequest;
import com.sinwoo.platform.leave.dto.LeaveResponse;

public interface LeaveReqService {

    /** 휴가 신청 컨텍스트 (신청자 정보, 잔여일수, 옵션 목록, 조직도, 직원 목록) */
    LeaveResponse.Context getContext(AuthenticatedUsr usr);

    /** 내 휴가 신청 목록 (잔여일수 포함) */
    LeaveResponse.ItemList getMyLeaves(AuthenticatedUsr usr, String startDateFrom, String startDateTo, String status);

    /** 휴가 신청 단건 상세 */
    LeaveResponse.Item getLeave(AuthenticatedUsr usr, Long leaveId);

    /** 휴가 신청 생성 (임시저장 or 요청) */
    LeaveResponse.Item createLeave(AuthenticatedUsr usr, LeaveRequest request);

    /** 휴가 신청 수정 (임시저장 상태만 가능) */
    LeaveResponse.Item updateLeave(AuthenticatedUsr usr, Long leaveId, LeaveRequest request);

    /** 휴가 신청 삭제 (임시저장 상태만 가능, soft delete) */
    void deleteLeave(AuthenticatedUsr usr, Long leaveId);

    /** 휴가 신청 취소 (요청/승인 상태에서 가능) */
    LeaveResponse.Item cancelLeave(AuthenticatedUsr usr, Long leaveId);

    /** 휴가 일수 계산 (중복 검사 포함) */
    LeaveResponse.CalcResult calculateDays(AuthenticatedUsr usr, LeaveRequest.Calc request);

    /** 휴가 신청 승인 (결재자만, REQ 상태 → 본인 라인 APR 처리, 모든 라인 APR이면 LeaveReq APR로 전이) */
    LeaveResponse.Item confirmLeave(AuthenticatedUsr usr, Long leaveId);

    /** 휴가 신청 반려 (결재자만, REQ 상태 → 본인 라인 REJ 처리, LeaveReq REJ + 사유 저장) */
    LeaveResponse.Item rejectLeave(AuthenticatedUsr usr, Long leaveId, String rejectReason);
}
