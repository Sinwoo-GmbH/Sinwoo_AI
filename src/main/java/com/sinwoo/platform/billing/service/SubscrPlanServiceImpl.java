package com.sinwoo.platform.billing.service;

import com.sinwoo.platform.billing.domain.SubscrPlan;
import com.sinwoo.platform.billing.dto.CreateSubscrPlanRequest;
import com.sinwoo.platform.billing.dto.SubscrPlanListResponse;
import com.sinwoo.platform.billing.dto.SubscrPlanResponse;
import com.sinwoo.platform.billing.repository.SubscrPlanRepository;
import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SubscrPlanServiceImpl implements SubscrPlanService {

    private final SubscrPlanRepository subscrPlanRepository;

    @Override
    @Transactional
    public SubscrPlanResponse createSubscrPlan(CreateSubscrPlanRequest request) {
        String normalizedPlanCd = request.planCd().trim().toUpperCase();

        if (subscrPlanRepository.existsByPlanCdIgnoreCase(normalizedPlanCd)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Plan code already exists");
        }

        SubscrPlan plan = SubscrPlan.create(
                normalizedPlanCd,
                request.planNm().trim(),
                request.tenantTpCd().trim().toUpperCase(),
                request.billCyclCd().trim().toUpperCase(),
                request.currCd().trim().toUpperCase(),
                request.baseAmt() == null ? BigDecimal.ZERO : request.baseAmt(),
                request.usrLmtCnt(),
                normalizeYn(request.useYn(), "Y")
        );

        return SubscrPlanResponse.from(subscrPlanRepository.save(plan));
    }

    @Override
    public SubscrPlanListResponse getSubscrPlans() {
        List<SubscrPlanResponse> items = subscrPlanRepository.findAllByOrderByTenantTpCdAscBaseAmtAscIdAsc().stream()
                .map(SubscrPlanResponse::from)
                .toList();

        return new SubscrPlanListResponse(items.size(), items);
    }

    private String normalizeYn(String value, String defaultValue) {
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        return "Y".equalsIgnoreCase(value.trim()) ? "Y" : "N";
    }
}
