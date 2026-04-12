package com.sinwoo.company.dto;

import com.sinwoo.company.domain.Company;
import java.time.OffsetDateTime;

public record CompanyResponse(
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
    public static CompanyResponse from(Company company) {
        return new CompanyResponse(
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
