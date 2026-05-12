package com.sinwoo.platform.company.service;

import static com.sinwoo.common.util.StringNormalizer.blankToNull;
import static com.sinwoo.common.util.StringNormalizer.defaultIfBlankUpper;
import static com.sinwoo.common.util.StringNormalizer.trimAndUpper;

import com.sinwoo.platform.company.domain.Co;
import com.sinwoo.platform.company.dto.CoListResponse;
import com.sinwoo.platform.company.dto.CoResponse;
import com.sinwoo.platform.company.dto.CreateCoRequest;
import com.sinwoo.platform.company.repository.CoRepository;
import com.sinwoo.platform.support.PlatformRefValidator;
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
    private final PlatformRefValidator refValidator;

    @Override
    @Transactional
    public CoResponse createCo(CreateCoRequest request) {
        refValidator.requireTenantExists(request.tenantId());
        String normalizedCoCd = trimAndUpper(request.coCd());
        String normalizedStsCd = defaultIfBlankUpper(request.stsCd(), "ACTIVE");

        if (coRepository.existsByTenantIdAndCoCdIgnoreCase(request.tenantId(), normalizedCoCd)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Co code already exists in tenant");
        }

        Co company = Co.create(
                request.tenantId(),
                normalizedCoCd,
                request.coNm().trim(),
                blankToNull(request.regNo()),
                normalizedStsCd
        );

        return CoResponse.from(coRepository.save(company));
    }

    @Override
    public CoListResponse getCos(Long tenantId) {
        refValidator.requireTenantExists(tenantId);

        List<CoResponse> items = coRepository.findAllByTenantIdOrderByCreatedAtDescIdDesc(tenantId).stream()
                .map(CoResponse::from)
                .toList();

        return new CoListResponse(items.size(), items);
    }
}
