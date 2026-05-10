package com.sinwoo.platform.mnu.service;

import com.sinwoo.platform.mnu.dto.RoleMnuAuthListResponse;
import com.sinwoo.platform.mnu.dto.UpsertRoleMnuAuthRequest;

public interface RoleMnuAuthService {

    RoleMnuAuthListResponse upsertRoleMnuAuths(UpsertRoleMnuAuthRequest request);

    RoleMnuAuthListResponse getRoleMnuAuths(String roleCd);
}
