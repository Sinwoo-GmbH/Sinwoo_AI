package com.sinwoo.platform.worklocation.dto;

import com.sinwoo.platform.worklocation.domain.WorkLoc;
import java.time.OffsetDateTime;

public record WorkLocResponse(
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
    public static WorkLocResponse from(WorkLoc workLoc) {
        return new WorkLocResponse(
                workLoc.getId(),
                workLoc.getTenantId(),
                workLoc.getCoId(),
                workLoc.getWorkLocCd(),
                workLoc.getWorkLocNm(),
                workLoc.getClntCoNm(),
                workLoc.getCtryCd(),
                workLoc.getRegionCd(),
                workLoc.getCityNm(),
                workLoc.getAddr1(),
                workLoc.getStsCd(),
                workLoc.getCreatedAt(),
                workLoc.getUpdatedAt()
        );
    }
}
