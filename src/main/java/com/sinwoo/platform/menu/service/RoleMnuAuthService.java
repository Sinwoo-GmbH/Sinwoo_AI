package com.sinwoo.platform.menu.service;

import com.sinwoo.platform.menu.dto.RoleMnuAuthListResponse;
import com.sinwoo.platform.menu.dto.UpsertRoleMnuAuthRequest;

public interface RoleMnuAuthService {

    RoleMnuAuthListResponse upsertRoleMnuAuths(UpsertRoleMnuAuthRequest request);

    RoleMnuAuthListResponse getRoleMnuAuths(String roleCd);
}
