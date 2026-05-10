package com.sinwoo.platform.code.dto;

import com.sinwoo.platform.code.domain.CdGroup;
import java.time.OffsetDateTime;

public record CdGroupResponse(
        Long grpId,
        String grpCd,
        String grpNmKo,
        String grpNmEn,
        String grpNmDe,
        String dspGrpNm,
        String sysYn,
        String useYn,
        Integer dspOrd,
        OffsetDateTime crtDtm,
        OffsetDateTime updDtm
) {
    public static CdGroupResponse from(CdGroup group, String dspGrpNm) {
        return new CdGroupResponse(
                group.getId(),
                group.getGrpCd(),
                group.getGrpNmKo(),
                group.getGrpNmEn(),
                group.getGrpNmDe(),
                dspGrpNm,
                group.getSysYn(),
                group.getUseYn(),
                group.getDspOrd(),
                group.getCreatedAt(),
                group.getUpdatedAt()
        );
    }
}
