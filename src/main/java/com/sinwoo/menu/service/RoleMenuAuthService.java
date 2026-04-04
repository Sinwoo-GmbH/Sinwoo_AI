package com.sinwoo.menu.service;

import com.sinwoo.menu.dto.RoleMenuAuthListResponse;
import com.sinwoo.menu.dto.UpsertRoleMenuAuthRequest;

public interface RoleMenuAuthService {

    RoleMenuAuthListResponse upsertRoleMenuAuths(UpsertRoleMenuAuthRequest request);

    RoleMenuAuthListResponse getRoleMenuAuths(String roleCd);
}
