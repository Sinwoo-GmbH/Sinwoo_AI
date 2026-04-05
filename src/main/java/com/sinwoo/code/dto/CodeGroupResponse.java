package com.sinwoo.code.dto;

import com.sinwoo.code.domain.CodeGroup;
import java.time.OffsetDateTime;

public record CodeGroupResponse(
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
    public static CodeGroupResponse from(CodeGroup group, String dspGrpNm) {
        return new CodeGroupResponse(
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
