package com.sinwoo.platform.department.service;

import com.sinwoo.platform.department.dto.CreateDeptRequest;
import com.sinwoo.platform.department.dto.DeptListResponse;
import com.sinwoo.platform.department.dto.DeptResponse;
import com.sinwoo.platform.department.dto.DeptTreeResponse;

public interface DeptService {

    DeptResponse createDept(CreateDeptRequest request);

    DeptListResponse getDepts(Long tenantId, Long coId);

    DeptTreeResponse getDeptTree(Long tenantId, Long coId);
}
