package com.sinwoo.platform.worklocation.service;

import com.sinwoo.common.security.AuthenticatedUsr;
import com.sinwoo.platform.worklocation.dto.CreateWorkLocRequest;
import com.sinwoo.platform.worklocation.dto.UpdateWorkLocRequest;
import com.sinwoo.platform.worklocation.dto.WorkLocListResponse;
import com.sinwoo.platform.worklocation.dto.WorkLocResponse;

public interface WorkLocService {

    WorkLocResponse createWorkLoc(AuthenticatedUsr authenticatedUsr, CreateWorkLocRequest request);

    WorkLocResponse updateWorkLoc(AuthenticatedUsr authenticatedUsr, Long workLocId, UpdateWorkLocRequest request);

    WorkLocListResponse getWorkLocs(AuthenticatedUsr authenticatedUsr, Long tenantId, Long coId);
}
