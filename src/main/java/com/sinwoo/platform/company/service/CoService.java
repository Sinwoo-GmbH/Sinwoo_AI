package com.sinwoo.platform.company.service;

import com.sinwoo.platform.company.dto.CoListResponse;
import com.sinwoo.platform.company.dto.CoResponse;
import com.sinwoo.platform.company.dto.CreateCoRequest;

public interface CoService {

    CoResponse createCo(CreateCoRequest request);

    CoListResponse getCos(Long tenantId);
}
