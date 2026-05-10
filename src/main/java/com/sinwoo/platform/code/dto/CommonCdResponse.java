package com.sinwoo.platform.code.dto;

import com.sinwoo.platform.code.domain.CommonCd;
import java.time.OffsetDateTime;

public record CommonCdResponse(
        Long cdId,
        Long grpId,
        String grpCd,
        String cd,
        String cdNmKo,
        String cdNmEn,
        String cdNmDe,
        String dspCdNm,
        String cdDescKo,
        String cdDescEn,
        String cdDescDe,
        String useYn,
        Integer dspOrd,
        OffsetDateTime crtDtm,
        OffsetDateTime updDtm
) {
    public static CommonCdResponse from(CommonCd code, String grpCd, String dspCdNm) {
        return new CommonCdResponse(
                code.getId(),
                code.getGrpId(),
                grpCd,
                code.getCd(),
                code.getCdNmKo(),
                code.getCdNmEn(),
                code.getCdNmDe(),
                dspCdNm,
                code.getCdDescKo(),
                code.getCdDescEn(),
                code.getCdDescDe(),
                code.getUseYn(),
                code.getDspOrd(),
                code.getCreatedAt(),
                code.getUpdatedAt()
        );
    }
}
