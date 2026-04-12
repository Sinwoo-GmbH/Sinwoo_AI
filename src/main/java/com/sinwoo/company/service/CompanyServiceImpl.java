package com.sinwoo.company.service;

import com.sinwoo.company.domain.Company;
import com.sinwoo.company.dto.CompanyListResponse;
import com.sinwoo.company.dto.CompanyResponse;
import com.sinwoo.company.dto.CreateCompanyRequest;
import com.sinwoo.company.repository.CompanyRepository;
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
public class CompanyServiceImpl implements CompanyService {

    private final CompanyRepository companyRepository;
    private final TenantRepository tenantRepository;

    @Override
    @Transactional
    public CompanyResponse createCompany(CreateCompanyRequest request) {
        validateTenant(request.tenantId());
        String normalizedCoCd = normalizeCode(request.coCd());
        String normalizedStsCd = normalizeStatus(request.stsCd());

        if (companyRepository.existsByTenantIdAndCoCdIgnoreCase(request.tenantId(), normalizedCoCd)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Company code already exists in tenant");
        }

        Company company = Company.create(
                request.tenantId(),
                normalizedCoCd,
                request.coNm().trim(),
                blankToNull(request.regNo()),
                normalizeCountryCode(request.hqCtryCd()),
                normalizeRegionCode(request.hqRegionCd()),
                blankToNull(request.hqCityNm()),
                blankToNull(request.hqAddr1()),
                normalizedStsCd
        );

        return CompanyResponse.from(companyRepository.save(company));
    }

    @Override
    public CompanyListResponse getCompanies(Long tenantId) {
        validateTenant(tenantId);

        List<CompanyResponse> items = companyRepository.findAllByTenantIdOrderByCreatedAtDescIdDesc(tenantId).stream()
                .map(CompanyResponse::from)
                .toList();

        return new CompanyListResponse(items.size(), items);
    }

    private void validateTenant(Long tenantId) {
        if (tenantId == null || !tenantRepository.existsById(tenantId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tenant not found");
        }
    }

    private String normalizeCode(String value) {
        return value.trim().toUpperCase();
    }

    private String normalizeStatus(String value) {
        if (value == null || value.isBlank()) {
            return "ACTIVE";
        }
        return value.trim().toUpperCase();
    }

    private String normalizeCountryCode(String value) {
        if (value == null || value.isBlank()) {
            return "DE";
        }
        return value.trim().toUpperCase();
    }

    private String normalizeRegionCode(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim().toUpperCase();
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
