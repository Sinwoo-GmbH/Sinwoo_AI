package com.sinwoo.employee.service;

import com.sinwoo.employee.dto.CreateEmployeeRequest;
import com.sinwoo.employee.dto.EmployeeListResponse;
import com.sinwoo.employee.dto.EmployeeResponse;

public interface EmployeeService {

    EmployeeResponse createEmployee(CreateEmployeeRequest request);

    EmployeeListResponse getEmployees(Long tenantId, Long coId, Long deptId);
}
