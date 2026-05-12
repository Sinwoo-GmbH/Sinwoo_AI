package com.sinwoo.platform.employee.service;

import com.sinwoo.platform.employee.dto.CreateEmpRequest;
import com.sinwoo.platform.employee.dto.EmpListResponse;
import com.sinwoo.platform.employee.dto.EmpResponse;

public interface EmpService {

    EmpResponse createEmp(CreateEmpRequest request);

    EmpListResponse getEmps(Long tenantId, Long coId, Long deptId);
}
