package com.sinwoo.tenant.service;

import com.sinwoo.tenant.dto.CreateTenantRequest;
import com.sinwoo.tenant.dto.TenantListResponse;
import com.sinwoo.tenant.dto.TenantResponse;

public interface TenantService {

    TenantResponse createTenant(CreateTenantRequest request);

    TenantListResponse getTenants();
}
