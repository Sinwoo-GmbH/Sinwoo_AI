package com.sinwoo.worklocation.service;

import com.sinwoo.common.security.AuthenticatedUser;
import com.sinwoo.worklocation.dto.CreateWorkLocationRequest;
import com.sinwoo.worklocation.dto.UpdateWorkLocationRequest;
import com.sinwoo.worklocation.dto.WorkLocationListResponse;
import com.sinwoo.worklocation.dto.WorkLocationResponse;

public interface WorkLocationService {

    WorkLocationResponse createWorkLocation(AuthenticatedUser authenticatedUser, CreateWorkLocationRequest request);

    WorkLocationResponse updateWorkLocation(AuthenticatedUser authenticatedUser, Long workLocId, UpdateWorkLocationRequest request);

    WorkLocationListResponse getWorkLocations(AuthenticatedUser authenticatedUser, Long tenantId, Long coId);
}
