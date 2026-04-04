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
        String normalizedTenantCd = normalizeTenantCd(request.tenantCd());
        String normalizedTenantTpCd = normalizeTenantType(request.tenantTpCd());
        String normalizedBillFreeYn = normalizeBillFreeYn(normalizedTenantTpCd, request.billFreeYn());

        if (tenantRepository.existsByTenantCdIgnoreCase(normalizedTenantCd)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Tenant code already exists");
        }

        Tenant tenant = Tenant.create(
                normalizedTenantCd,
                request.tenantNm().trim(),
                normalizedTenantTpCd,
                normalizedBillFreeYn,
                TenantStatus.ACTIVE
        );
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

    private String normalizeTenantCd(String tenantCd) {
        return tenantCd.trim().toUpperCase();
    }

    private String normalizeTenantType(String tenantTpCd) {
        if (tenantTpCd == null || tenantTpCd.isBlank()) {
            return "CUSTOMER";
        }
        return tenantTpCd.trim().toUpperCase();
    }

    private String normalizeBillFreeYn(String tenantTpCd, String billFreeYn) {
        if ("INTERNAL".equals(tenantTpCd)) {
            return "Y";
        }
        if (billFreeYn == null || billFreeYn.isBlank()) {
            return "N";
        }
        return "Y".equalsIgnoreCase(billFreeYn.trim()) ? "Y" : "N";
    }
}
