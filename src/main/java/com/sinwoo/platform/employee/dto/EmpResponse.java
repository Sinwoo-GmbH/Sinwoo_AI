package com.sinwoo.platform.emp.dto;

import com.sinwoo.platform.emp.domain.Emp;
import java.time.LocalDate;
import java.time.OffsetDateTime;

public record EmpResponse(
        Long empId,
        Long tenantId,
        Long coId,
        Long usrId,
        Long deptId,
        Long workLocId,
        Long mgrEmpId,
        String empNo,
        String empNm,
        String teamRoleCd,
        String jobTtlNm,
        LocalDate hireDt,
        LocalDate retrDt,
        String stsCd,
        OffsetDateTime crtDtm,
        OffsetDateTime updDtm
) {
    public static EmpResponse from(Emp emp) {
        return new EmpResponse(
                emp.getId(),
                emp.getTenantId(),
                emp.getCoId(),
                emp.getUsrId(),
                emp.getDeptId(),
                emp.getWorkLocId(),
                emp.getMgrEmpId(),
                emp.getEmpNo(),
                emp.getEmpNm(),
                emp.getTeamRoleCd(),
                emp.getJobTtlNm(),
                emp.getHireDt(),
                emp.getRetrDt(),
                emp.getStsCd(),
                emp.getCreatedAt(),
                emp.getUpdatedAt()
        );
    }
}
