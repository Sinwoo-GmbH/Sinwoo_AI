package com.sinwoo.platform.emp.service;

import com.sinwoo.platform.emp.dto.CreateEmpRequest;
import com.sinwoo.platform.emp.dto.EmpListResponse;
import com.sinwoo.platform.emp.dto.EmpResponse;

public interface EmpService {

    EmpResponse createEmp(CreateEmpRequest request);

    EmpListResponse getEmps(Long tenantId, Long coId, Long deptId);
}
