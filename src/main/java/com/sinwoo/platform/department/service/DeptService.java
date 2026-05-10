package com.sinwoo.platform.dept.service;

import com.sinwoo.platform.dept.dto.CreateDeptRequest;
import com.sinwoo.platform.dept.dto.DeptListResponse;
import com.sinwoo.platform.dept.dto.DeptResponse;
import com.sinwoo.platform.dept.dto.DeptTreeResponse;

public interface DeptService {

    DeptResponse createDept(CreateDeptRequest request);

    DeptListResponse getDepts(Long tenantId, Long coId);

    DeptTreeResponse getDeptTree(Long tenantId, Long coId);
}
