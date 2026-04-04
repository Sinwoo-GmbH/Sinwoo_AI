package com.sinwoo.department.service;

import com.sinwoo.department.dto.CreateDepartmentRequest;
import com.sinwoo.department.dto.DepartmentListResponse;
import com.sinwoo.department.dto.DepartmentResponse;
import com.sinwoo.department.dto.DepartmentTreeResponse;

public interface DepartmentService {

    DepartmentResponse createDepartment(CreateDepartmentRequest request);

    DepartmentListResponse getDepartments(Long tenantId, Long coId);

    DepartmentTreeResponse getDepartmentTree(Long tenantId, Long coId);
}
