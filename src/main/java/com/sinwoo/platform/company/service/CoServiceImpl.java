package com.sinwoo.platform.company.service;

import com.sinwoo.platform.company.domain.Co;
import com.sinwoo.platform.company.dto.CoListResponse;
import com.sinwoo.platform.company.dto.CoResponse;
import com.sinwoo.platform.company.dto.CreateCoRequest;
import com.sinwoo.platform.company.repository.CoRepository;
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
public class CoServiceImpl implements CoService {

    private final CoRepository coRepository;
    private final TenantRepository tenantRepository;

    @Override
    @Transactional
    public CoResponse createCo(CreateCoRequest request) {
        validateTenant(request.tenantId());
        String normalizedCoCd = normalizeCd(request.coCd());
        String normalizedStsCd = normalizeStatus(request.stsCd());

        if (coRepository.existsByTenantIdAndCoCdIgnoreCase(request.tenantId(), normalizedCoCd)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Co code already exists in tenant");
        }

        Co company = Co.create(
                request.tenantId(),
                normalizedCoCd,
                request.coNm().trim(),
                blankToNull(request.regNo()),
                normalizeCountryCd(request.hqCtryCd()),
                normalizeRegionCd(request.hqRegionCd()),
                blankToNull(request.hqCityNm()),
                blankToNull(request.hqAddr1()),
                normalizedStsCd
        );

        return CoResponse.from(coRepository.save(company));
    }

    @Override
    public CoListResponse getCos(Long tenantId) {
        validateTenant(tenantId);

        List<CoResponse> items = coRepository.findAllByTenantIdOrderByCreatedAtDescIdDesc(tenantId).stream()
                .map(CoResponse::from)
                .toList();

        return new CoListResponse(items.size(), items);
    }

    private void validateTenant(Long tenantId) {
        if (tenantId == null || !tenantRepository.existsById(tenantId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tenant not found");
        }
    }

    private String normalizeCd(String value) {
        return value.trim().toUpperCase();
    }

    private String normalizeStatus(String value) {
        if (value == null || value.isBlank()) {
            return "ACTIVE";
        }
        return value.trim().toUpperCase();
    }

    private String normalizeCountryCd(String value) {
        if (value == null || value.isBlank()) {
            return "DE";
        }
        return value.trim().toUpperCase();
    }

    private String normalizeRegionCd(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim().toUpperCase();
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
