package com.sinwoo.code.dto;

import com.sinwoo.code.domain.CommonCode;
import java.time.OffsetDateTime;

public record CommonCodeResponse(
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
    public static CommonCodeResponse from(CommonCode code, String grpCd, String dspCdNm) {
        return new CommonCodeResponse(
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
