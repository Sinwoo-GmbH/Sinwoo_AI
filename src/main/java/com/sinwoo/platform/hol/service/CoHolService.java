package com.sinwoo.platform.hol.service;

import com.sinwoo.common.security.AuthenticatedUsr;
import com.sinwoo.platform.hol.dto.CoHolListResponse;
import com.sinwoo.platform.hol.dto.CoHolResponse;
import com.sinwoo.platform.hol.dto.CreateCoHolRequest;
import com.sinwoo.platform.hol.dto.UpdateCoHolRequest;
import java.time.LocalDate;

public interface CoHolService {

    /** 회사 휴일 전체 목록 */
    CoHolListResponse getCoHols(AuthenticatedUsr usr);

    /** 기간 기반 회사 휴일 조회 (캘린더 뷰용) */
    CoHolListResponse getCoHolsByPeriod(AuthenticatedUsr usr, Short yr, LocalDate from, LocalDate to);

    /** 회사 휴일 등록 (customer admin) */
    CoHolResponse createCoHol(AuthenticatedUsr usr, CreateCoHolRequest request);

    /** 회사 휴일 수정 (customer admin) */
    CoHolResponse updateCoHol(AuthenticatedUsr usr, Long coHolId, UpdateCoHolRequest request);

    /** 회사 휴일 삭제 (customer admin, soft delete) */
    void deleteCoHol(AuthenticatedUsr usr, Long coHolId);
}
