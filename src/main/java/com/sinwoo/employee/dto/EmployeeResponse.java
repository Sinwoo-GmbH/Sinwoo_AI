package com.sinwoo.employee.dto;

import com.sinwoo.employee.domain.Employee;
import java.time.LocalDate;
import java.time.OffsetDateTime;

public record EmployeeResponse(
        Long empId,
        Long tenantId,
        Long coId,
        Long usrId,
        Long deptId,
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
    public static EmployeeResponse from(Employee employee) {
        return new EmployeeResponse(
                employee.getId(),
                employee.getTenantId(),
                employee.getCoId(),
                employee.getUsrId(),
                employee.getDeptId(),
                employee.getMgrEmpId(),
                employee.getEmpNo(),
                employee.getEmpNm(),
                employee.getTeamRoleCd(),
                employee.getJobTtlNm(),
                employee.getHireDt(),
                employee.getRetrDt(),
                employee.getStsCd(),
                employee.getCreatedAt(),
                employee.getUpdatedAt()
        );
    }
}
