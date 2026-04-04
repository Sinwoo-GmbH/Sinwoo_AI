package com.sinwoo.department.dto;

import com.sinwoo.department.domain.Department;
import java.time.OffsetDateTime;

public record DepartmentResponse(
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
    public static DepartmentResponse from(Department department) {
        return new DepartmentResponse(
                department.getId(),
                department.getTenantId(),
                department.getCoId(),
                department.getDeptCd(),
                department.getDeptNm(),
                department.getUpDeptId(),
                department.getDeptLvlNo(),
                department.getStsCd(),
                department.getCreatedAt(),
                department.getUpdatedAt()
        );
    }
}
