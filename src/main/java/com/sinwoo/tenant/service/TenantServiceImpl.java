package com.sinwoo.tenant.service;

import com.sinwoo.tenant.domain.Tenant;
import com.sinwoo.tenant.domain.TenantStatus;
import com.sinwoo.tenant.dto.CreateTenantRequest;
import com.sinwoo.tenant.dto.TenantListResponse;
import com.sinwoo.tenant.dto.TenantResponse;
import com.sinwoo.tenant.repository.TenantRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TenantServiceImpl implements TenantService {

    private final TenantRepository tenantRepository;

    @Override
    @Transactional
    public TenantResponse createTenant(CreateTenantRequest request) {
        String normalizedCode = normalizeCode(request.code());

        if (tenantRepository.existsByCodeIgnoreCase(normalizedCode)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Tenant code already exists");
        }

        Tenant tenant = Tenant.create(normalizedCode, request.name().trim(), TenantStatus.ACTIVE);
        Tenant savedTenant = tenantRepository.save(tenant);
        return TenantResponse.from(savedTenant);
    }

    @Override
    public TenantListResponse getTenants() {
        List<TenantResponse> items = tenantRepository.findAllByOrderByCreatedAtDescIdDesc().stream()
                .map(TenantResponse::from)
                .toList();

        return new TenantListResponse(items.size(), items);
    }

    private String normalizeCode(String code) {
        return code.trim().toUpperCase();
    }
}
