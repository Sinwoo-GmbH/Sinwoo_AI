package com.sinwoo.worklocation.dto;

import com.sinwoo.worklocation.domain.WorkLocation;
import java.time.OffsetDateTime;

public record WorkLocationResponse(
        Long workLocId,
        Long tenantId,
        Long coId,
        String workLocCd,
        String workLocNm,
        String clntCoNm,
        String ctryCd,
        String regionCd,
        String cityNm,
        String addr1,
        String stsCd,
        OffsetDateTime crtDtm,
        OffsetDateTime updDtm
) {
    public static WorkLocationResponse from(WorkLocation workLocation) {
        return new WorkLocationResponse(
                workLocation.getId(),
                workLocation.getTenantId(),
                workLocation.getCoId(),
                workLocation.getWorkLocCd(),
                workLocation.getWorkLocNm(),
                workLocation.getClntCoNm(),
                workLocation.getCtryCd(),
                workLocation.getRegionCd(),
                workLocation.getCityNm(),
                workLocation.getAddr1(),
                workLocation.getStsCd(),
                workLocation.getCreatedAt(),
                workLocation.getUpdatedAt()
        );
    }
}
