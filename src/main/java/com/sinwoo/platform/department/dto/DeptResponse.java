package com.sinwoo.platform.dept.dto;

import com.sinwoo.platform.dept.domain.Dept;
import java.time.OffsetDateTime;

public record DeptResponse(
        Long deptId,
        Long tenantId,
        Long coId,
        String deptCd,
        String deptNm,
        Long upDeptId,
        Integer deptLvlNo,
        String stsCd,
        OffsetDateTime crtDtm,
        OffsetDateTime updDtm
) {
    public static DeptResponse from(Dept dept) {
        return new DeptResponse(
                dept.getId(),
                dept.getTenantId(),
                dept.getCoId(),
                dept.getDeptCd(),
                dept.getDeptNm(),
                dept.getUpDeptId(),
                dept.getDeptLvlNo(),
                dept.getStsCd(),
                dept.getCreatedAt(),
                dept.getUpdatedAt()
        );
    }
}
