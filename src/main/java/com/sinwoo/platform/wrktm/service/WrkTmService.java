package com.sinwoo.platform.wrktm.service;

import com.sinwoo.common.security.AuthenticatedUsr;
import com.sinwoo.platform.wrktm.dto.ClockInRequest;
import com.sinwoo.platform.wrktm.dto.ClockOutRequest;
import com.sinwoo.platform.wrktm.dto.SaveWrkTmRequest;
import com.sinwoo.platform.wrktm.dto.WrkTmListResponse;
import com.sinwoo.platform.wrktm.dto.WrkTmResponse;
import java.time.LocalDate;

public interface WrkTmService {

    /** Dashboard: 출근 등록 (Now or 임의 시간) */
    WrkTmResponse clockIn(AuthenticatedUsr usr, ClockInRequest request);

    /** Dashboard: 퇴근 등록 (Now or 임의 시간) */
    WrkTmResponse clockOut(AuthenticatedUsr usr, ClockOutRequest request);

    /** My Working Time: 출퇴근 저장 (생성 or 수정) */
    WrkTmResponse saveWrkTm(AuthenticatedUsr usr, SaveWrkTmRequest request);

    /** My Working Time: 출퇴근 삭제 (soft delete) */
    void deleteWrkTm(AuthenticatedUsr usr, Long wrkTmId);

    /** My Working Time: 특정 날짜 조회 */
    WrkTmResponse getWrkTm(AuthenticatedUsr usr, LocalDate workDt);

    /** My Working Time: 기간 조회 (캘린더 월간 뷰) */
    WrkTmListResponse getMyWrkTms(AuthenticatedUsr usr, LocalDate from, LocalDate to);

    /** Report: 전직원 기간 조회 (관리자) */
    WrkTmListResponse getAllWrkTms(AuthenticatedUsr usr, LocalDate from, LocalDate to);
}
