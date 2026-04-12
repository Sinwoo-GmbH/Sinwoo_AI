package com.sinwoo.worklocation.service;

import com.sinwoo.common.security.AuthenticatedUser;
import com.sinwoo.company.repository.CompanyRepository;
import com.sinwoo.tenant.repository.TenantRepository;
import com.sinwoo.worklocation.domain.WorkLocation;
import com.sinwoo.worklocation.dto.CreateWorkLocationRequest;
import com.sinwoo.worklocation.dto.UpdateWorkLocationRequest;
import com.sinwoo.worklocation.dto.WorkLocationListResponse;
import com.sinwoo.worklocation.dto.WorkLocationResponse;
import com.sinwoo.worklocation.repository.WorkLocationRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WorkLocationServiceImpl implements WorkLocationService {

    private final WorkLocationRepository workLocationRepository;
    private final TenantRepository tenantRepository;
    private final CompanyRepository companyRepository;

    @Override
    @Transactional
    public WorkLocationResponse createWorkLocation(AuthenticatedUser authenticatedUser, CreateWorkLocationRequest request) {
        AccessScope scope = resolveAccessScope(authenticatedUser, request.tenantId(), request.coId());
        String normalizedWorkLocCd = normalizeCode(request.workLocCd());

        if (workLocationRepository.existsByTenantIdAndCoIdAndWorkLocCdIgnoreCase(scope.tenantId(), scope.coId(), normalizedWorkLocCd)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Work location code already exists in company");
        }

        WorkLocation workLocation = WorkLocation.create(
                scope.tenantId(),
                scope.coId(),
                normalizedWorkLocCd,
                request.workLocNm().trim(),
                blankToNull(request.clntCoNm()),
                normalizeCountryCode(request.ctryCd()),
                normalizeRegionCode(request.regionCd()),
                blankToNull(request.cityNm()),
                blankToNull(request.addr1()),
                normalizeStatus(request.stsCd())
        );

        return WorkLocationResponse.from(workLocationRepository.save(workLocation));
    }

    @Override
    @Transactional
    public WorkLocationResponse updateWorkLocation(AuthenticatedUser authenticatedUser, Long workLocId, UpdateWorkLocationRequest request) {
        AccessScope scope = resolveAccessScope(authenticatedUser, request.tenantId(), request.coId());
        WorkLocation workLocation = workLocationRepository.findByIdAndTenantIdAndCoId(workLocId, scope.tenantId(), scope.coId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Work location not found"));

        workLocation.update(
                request.workLocNm().trim(),
                blankToNull(request.clntCoNm()),
                normalizeCountryCode(request.ctryCd()),
                normalizeRegionCode(request.regionCd()),
                blankToNull(request.cityNm()),
                blankToNull(request.addr1()),
                normalizeStatus(request.stsCd())
        );

        return WorkLocationResponse.from(workLocationRepository.save(workLocation));
    }

    @Override
    public WorkLocationListResponse getWorkLocations(AuthenticatedUser authenticatedUser, Long tenantId, Long coId) {
        AccessScope scope = resolveAccessScope(authenticatedUser, tenantId, coId);

        List<WorkLocationResponse> items = workLocationRepository.findAllByTenantIdAndCoIdOrderByWorkLocNmAscIdAsc(scope.tenantId(), scope.coId())
                .stream()
                .map(WorkLocationResponse::from)
                .toList();

        return new WorkLocationListResponse(items.size(), items);
    }

    private AccessScope resolveAccessScope(AuthenticatedUser authenticatedUser, Long tenantId, Long coId) {
        if (authenticatedUser == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication is required");
        }

        boolean superAdmin = authenticatedUser.roleCds() != null && authenticatedUser.roleCds().contains("ROLE_PLATFORM_SUPER_ADMIN");
        if (superAdmin) {
            validateTenant(tenantId);
            validateCompany(tenantId, coId);
            return new AccessScope(tenantId, coId);
        }

        if (!hasCustomerAdminRole(authenticatedUser)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Customer admin access is required");
        }

        if (authenticatedUser.tenantId() == null || authenticatedUser.coId() == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Company context is required");
        }

        if (tenantId != null && !authenticatedUser.tenantId().equals(tenantId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cross-tenant access is not allowed");
        }

        if (coId != null && !authenticatedUser.coId().equals(coId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cross-company access is not allowed");
        }

        validateTenant(authenticatedUser.tenantId());
        validateCompany(authenticatedUser.tenantId(), authenticatedUser.coId());
        return new AccessScope(authenticatedUser.tenantId(), authenticatedUser.coId());
    }

    private boolean hasCustomerAdminRole(AuthenticatedUser authenticatedUser) {
        return authenticatedUser.roleCds() != null && authenticatedUser.roleCds().stream()
                .anyMatch(roleCd -> "ROLE_CUSTOMER_ADMIN_MEMBER".equals(roleCd) || "ROLE_CUSTOMER_ADMIN_LEADER".equals(roleCd));
    }

    private void validateTenant(Long tenantId) {
        if (tenantId == null || !tenantRepository.existsById(tenantId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tenant not found");
        }
    }

    private void validateCompany(Long tenantId, Long coId) {
        if (coId == null || companyRepository.findByIdAndTenantId(coId, tenantId).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Company not found in tenant");
        }
    }

    private String normalizeCode(String value) {
        return value.trim().toUpperCase();
    }

    private String normalizeCountryCode(String value) {
        return value.trim().toUpperCase();
    }

    private String normalizeRegionCode(String value) {
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
