package com.sinwoo.platform.auth.service;

import com.sinwoo.platform.auth.dto.CreateRoleRequest;
import com.sinwoo.platform.auth.dto.RoleListResponse;
import com.sinwoo.platform.auth.dto.RoleResponse;
import java.util.List;

public interface RoleService {

    RoleResponse createRole(CreateRoleRequest request);

    RoleListResponse getRoles();

    List<RoleResponse> getRolesByCds(List<String> roleCds);
}
