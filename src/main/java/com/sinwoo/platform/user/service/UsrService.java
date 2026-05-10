package com.sinwoo.platform.user.service;

import com.sinwoo.platform.user.dto.CreateUsrRequest;
import com.sinwoo.platform.user.dto.UsrListResponse;
import com.sinwoo.platform.user.dto.UsrResponse;

public interface UsrService {

    UsrResponse createUsr(CreateUsrRequest request);

    UsrListResponse getUsrs(Long tenantId, Long coId);
}
