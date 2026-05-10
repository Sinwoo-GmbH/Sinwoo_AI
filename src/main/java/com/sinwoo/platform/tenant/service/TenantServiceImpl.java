package com.sinwoo.platform.tenant.service;

import com.sinwoo.platform.tenant.domain.Tenant;
import com.sinwoo.platform.tenant.domain.TenantStatus;
import com.sinwoo.platform.tenant.dto.CreateTenantRequest;
import com.sinwoo.platform.tenant.dto.TenantListResponse;
import com.sinwoo.platform.tenant.dto.TenantResponse;
import com.sinwoo.platform.tenant.repository.TenantRepository;
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
        String normalizedEmailDomain = normalizeEmailDomain(request.emlDomn());
        String normalizedTenantTpCd = normalizeTenantType(request.tenantTpCd());
        String normalizedBillFreeYn = normalizeBillFreeYn(normalizedTenantTpCd, request.billFreeYn());

        if (tenantRepository.existsByTenantCdIgnoreCase(normalizedTenantCd)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Tenant code already exists");
        }
        if (normalizedEmailDomain != null && tenantRepository.existsByEmlDomnIgnoreCase(normalizedEmailDomain)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Tenant email domain already exists");
        }

        Tenant tenant = Tenant.create(
                normalizedTenantCd,
                request.tenantNm().trim(),
                normalizedEmailDomain,
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

    private String normalizeEmailDomain(String emlDomn) {
        if (emlDomn == null || emlDomn.isBlank()) {
            return null;
        }
        String normalized = emlDomn.trim().toLowerCase();
        if (normalized.startsWith("@")) {
            normalized = normalized.substring(1);
        }
        return normalized;
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
