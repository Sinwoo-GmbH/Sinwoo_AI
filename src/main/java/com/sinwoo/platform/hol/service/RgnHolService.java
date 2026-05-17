package com.sinwoo.platform.hol.service;

import com.sinwoo.common.security.AuthenticatedUsr;
import com.sinwoo.platform.hol.dto.RgnHolListResponse;
import java.time.LocalDate;

public interface RgnHolService {

    /** 로그인 사용자 부서의 지역 기반 공휴일 조회 (연도별) */
    RgnHolListResponse getMyRgnHols(AuthenticatedUsr usr, Short yr);

    /** 기간 기반 공휴일 조회 (캘린더 뷰용) */
    RgnHolListResponse getMyRgnHolsByPeriod(AuthenticatedUsr usr, LocalDate from, LocalDate to);

    /** 해당 연도 공휴일이 없으면 오픈 API에서 수집 */
    void syncRgnHols(Short yr);
}
