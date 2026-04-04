package com.sinwoo.auth.service;

import com.sinwoo.auth.dto.CreateRoleRequest;
import com.sinwoo.auth.dto.RoleListResponse;
import com.sinwoo.auth.dto.RoleResponse;
import java.util.List;

public interface RoleService {

    RoleResponse createRole(CreateRoleRequest request);

    RoleListResponse getRoles();

    List<RoleResponse> getRolesByCodes(List<String> roleCds);
}
