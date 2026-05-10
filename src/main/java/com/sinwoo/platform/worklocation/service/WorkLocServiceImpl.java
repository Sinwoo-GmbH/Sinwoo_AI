package com.sinwoo.platform.worklocation.service;

import com.sinwoo.common.security.AuthenticatedUsr;
import com.sinwoo.platform.company.repository.CoRepository;
import com.sinwoo.platform.tenant.repository.TenantRepository;
import com.sinwoo.platform.worklocation.domain.WorkLoc;
import com.sinwoo.platform.worklocation.dto.CreateWorkLocRequest;
import com.sinwoo.platform.worklocation.dto.UpdateWorkLocRequest;
import com.sinwoo.platform.worklocation.dto.WorkLocListResponse;
import com.sinwoo.platform.worklocation.dto.WorkLocResponse;
import com.sinwoo.platform.worklocation.repository.WorkLocRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WorkLocServiceImpl implements WorkLocService {

    private final WorkLocRepository workLocRepository;
    private final TenantRepository tenantRepository;
    private final CoRepository coRepository;

    @Override
    @Transactional
    public WorkLocResponse createWorkLoc(AuthenticatedUsr authenticatedUsr, CreateWorkLocRequest request) {
        AccessScope scope = resolveAccessScope(authenticatedUsr, request.tenantId(), request.coId());
        String normalizedWorkLocCd = normalizeCd(request.workLocCd());

        if (workLocRepository.existsByTenantIdAndCoIdAndWorkLocCdIgnoreCase(scope.tenantId(), scope.coId(), normalizedWorkLocCd)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Work location code already exists in company");
        }

        WorkLoc workLoc = WorkLoc.create(
                scope.tenantId(),
                scope.coId(),
                normalizedWorkLocCd,
                request.workLocNm().trim(),
                blankToNull(request.clntCoNm()),
                normalizeCountryCd(request.ctryCd()),
                normalizeRegionCd(request.regionCd()),
                blankToNull(request.cityNm()),
                blankToNull(request.addr1()),
                normalizeStatus(request.stsCd())
        );

        return WorkLocResponse.from(workLocRepository.save(workLoc));
    }

    @Override
    @Transactional
    public WorkLocResponse updateWorkLoc(AuthenticatedUsr authenticatedUsr, Long workLocId, UpdateWorkLocRequest request) {
        AccessScope scope = resolveAccessScope(authenticatedUsr, request.tenantId(), request.coId());
        WorkLoc workLoc = workLocRepository.findByIdAndTenantIdAndCoId(workLocId, scope.tenantId(), scope.coId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Work location not found"));

        workLoc.update(
                request.workLocNm().trim(),
                blankToNull(request.clntCoNm()),
                normalizeCountryCd(request.ctryCd()),
                normalizeRegionCd(request.regionCd()),
                blankToNull(request.cityNm()),
                blankToNull(request.addr1()),
                normalizeStatus(request.stsCd())
        );

        return WorkLocResponse.from(workLocRepository.save(workLoc));
    }

    @Override
    public WorkLocListResponse getWorkLocs(AuthenticatedUsr authenticatedUsr, Long tenantId, Long coId) {
        AccessScope scope = resolveAccessScope(authenticatedUsr, tenantId, coId);

        List<WorkLocResponse> items = workLocRepository.findAllByTenantIdAndCoIdOrderByWorkLocNmAscIdAsc(scope.tenantId(), scope.coId())
                .stream()
                .map(WorkLocResponse::from)
                .toList();

        return new WorkLocListResponse(items.size(), items);
    }

    private AccessScope resolveAccessScope(AuthenticatedUsr authenticatedUsr, Long tenantId, Long coId) {
        if (authenticatedUsr == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication is required");
        }

        boolean superAdmin = authenticatedUsr.roleCds() != null && authenticatedUsr.roleCds().contains("ROLE_PLATFORM_SUPER_ADMIN");
        if (superAdmin) {
            validateTenant(tenantId);
            validateCo(tenantId, coId);
            return new AccessScope(tenantId, coId);
        }

        if (!hasCustomerAdminRole(authenticatedUsr)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Customer admin access is required");
        }

        if (authenticatedUsr.tenantId() == null || authenticatedUsr.coId() == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Co context is required");
        }

        if (tenantId != null && !authenticatedUsr.tenantId().equals(tenantId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cross-tenant access is not allowed");
        }

        if (coId != null && !authenticatedUsr.coId().equals(coId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cross-company access is not allowed");
        }

        validateTenant(authenticatedUsr.tenantId());
        validateCo(authenticatedUsr.tenantId(), authenticatedUsr.coId());
        return new AccessScope(authenticatedUsr.tenantId(), authenticatedUsr.coId());
    }

    private boolean hasCustomerAdminRole(AuthenticatedUsr authenticatedUsr) {
        return authenticatedUsr.roleCds() != null && authenticatedUsr.roleCds().stream()
                .anyMatch(roleCd -> "ROLE_CUSTOMER_ADMIN_MEMBER".equals(roleCd) || "ROLE_CUSTOMER_ADMIN_LEADER".equals(roleCd));
    }

    private void validateTenant(Long tenantId) {
        if (tenantId == null || !tenantRepository.existsById(tenantId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tenant not found");
        }
    }

    private void validateCo(Long tenantId, Long coId) {
        if (coId == null || coRepository.findByIdAndTenantId(coId, tenantId).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Co not found in tenant");
        }
    }

    private String normalizeCd(String value) {
        return value.trim().toUpperCase();
    }

    private String normalizeCountryCd(String value) {
        return value.trim().toUpperCase();
    }

    private String normalizeRegionCd(String value) {
        return value.trim().toUpperCase();
    }

    private String normalizeStatus(String value) {
        if (value == null || value.isBlank()) {
            return "ACTIVE";
        }
        return value.trim().toUpperCase();
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private record AccessScope(Long tenantId, Long coId) {
    }
}
