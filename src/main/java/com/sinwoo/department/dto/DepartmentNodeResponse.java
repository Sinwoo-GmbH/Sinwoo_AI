package com.sinwoo.department.dto;

import com.sinwoo.department.domain.Department;
import java.util.ArrayList;
import java.util.List;

public record DepartmentNodeResponse(
        Long deptId,
        Long tenantId,
        Long coId,
        String deptCd,
        String deptNm,
        Long upDeptId,
        Integer deptLvlNo,
        String stsCd,
        List<DepartmentNodeResponse> childList
) {
    public static DepartmentNodeResponse from(Department department) {
        return new DepartmentNodeResponse(
                department.getId(),
                department.getTenantId(),
                department.getCoId(),
                department.getDeptCd(),
                department.getDeptNm(),
                department.getUpDeptId(),
                department.getDeptLvlNo(),
                department.getStsCd(),
                new ArrayList<>()
        );
    }
}
