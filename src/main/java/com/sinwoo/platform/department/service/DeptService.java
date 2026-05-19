package com.sinwoo.platform.department.service;

import com.sinwoo.common.security.AuthenticatedUsr;
import com.sinwoo.platform.department.dto.DeptRequest;
import com.sinwoo.platform.department.dto.DeptResponse;

public interface DeptService {

    DeptResponse createDept(AuthenticatedUsr usr, DeptRequest request);

    DeptResponse updateDept(AuthenticatedUsr usr, Long deptId, DeptRequest request);

    void deleteDept(AuthenticatedUsr usr, Long deptId);

    DeptResponse.ListWrap getDepts(AuthenticatedUsr usr);

    DeptResponse.TreeWrap getDeptTree(AuthenticatedUsr usr);

    DeptResponse.EmpCount getEmpCount(AuthenticatedUsr usr, Long deptId);
}
