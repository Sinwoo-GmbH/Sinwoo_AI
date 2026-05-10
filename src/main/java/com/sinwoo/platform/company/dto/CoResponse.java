package com.sinwoo.platform.company.dto;

import com.sinwoo.platform.company.domain.Co;
import java.time.OffsetDateTime;

public record CoResponse(
        Long coId,
        Long tenantId,
        String coCd,
        String coNm,
        String regNo,
        String hqCtryCd,
        String hqRegionCd,
        String hqCityNm,
        String hqAddr1,
        String stsCd,
        OffsetDateTime crtDtm,
        OffsetDateTime updDtm
) {
    public static CoResponse from(Co company) {
        return new CoResponse(
                company.getId(),
                company.getTenantId(),
                company.getCoCd(),
                company.getCoNm(),
                company.getRegNo(),
                company.getHqCtryCd(),
                company.getHqRegionCd(),
                company.getHqCityNm(),
                company.getHqAddr1(),
                company.getStsCd(),
                company.getCreatedAt(),
                company.getUpdatedAt()
        );
    }
}
