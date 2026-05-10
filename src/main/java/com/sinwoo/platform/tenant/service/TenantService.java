package com.sinwoo.platform.tenant.service;

import com.sinwoo.platform.tenant.dto.CreateTenantRequest;
import com.sinwoo.platform.tenant.dto.TenantListResponse;
import com.sinwoo.platform.tenant.dto.TenantResponse;

public interface TenantService {

    TenantResponse createTenant(CreateTenantRequest request);

    TenantListResponse getTenants();
}
