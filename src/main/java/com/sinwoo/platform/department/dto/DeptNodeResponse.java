package com.sinwoo.platform.dept.dto;

import com.sinwoo.platform.dept.domain.Dept;
import java.util.ArrayList;
import java.util.List;

public record DeptNodeResponse(
        Long deptId,
        Long tenantId,
        Long coId,
        String deptCd,
        String deptNm,
        Long upDeptId,
        Integer deptLvlNo,
        String stsCd,
        List<DeptNodeResponse> childList
) {
    public static DeptNodeResponse from(Dept dept) {
        return new DeptNodeResponse(
                dept.getId(),
                dept.getTenantId(),
                dept.getCoId(),
                dept.getDeptCd(),
                dept.getDeptNm(),
                dept.getUpDeptId(),
                dept.getDeptLvlNo(),
                dept.getStsCd(),
                new ArrayList<>()
        );
    }
}
