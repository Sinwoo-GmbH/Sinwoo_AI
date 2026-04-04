package com.sinwoo.billing.service;

import com.sinwoo.billing.domain.SubscriptionPlan;
import com.sinwoo.billing.dto.CreateSubscriptionPlanRequest;
import com.sinwoo.billing.dto.SubscriptionPlanListResponse;
import com.sinwoo.billing.dto.SubscriptionPlanResponse;
import com.sinwoo.billing.repository.SubscriptionPlanRepository;
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
public class SubscriptionPlanServiceImpl implements SubscriptionPlanService {

    private final SubscriptionPlanRepository subscriptionPlanRepository;

    @Override
    @Transactional
    public SubscriptionPlanResponse createSubscriptionPlan(CreateSubscriptionPlanRequest request) {
        String normalizedPlanCd = request.planCd().trim().toUpperCase();

        if (subscriptionPlanRepository.existsByPlanCdIgnoreCase(normalizedPlanCd)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Plan code already exists");
        }

        SubscriptionPlan plan = SubscriptionPlan.create(
                normalizedPlanCd,
                request.planNm().trim(),
                request.tenantTpCd().trim().toUpperCase(),
                request.billCyclCd().trim().toUpperCase(),
                request.currCd().trim().toUpperCase(),
                request.baseAmt() == null ? BigDecimal.ZERO : request.baseAmt(),
                request.usrLmtCnt(),
                normalizeYn(request.useYn(), "Y")
        );

        return SubscriptionPlanResponse.from(subscriptionPlanRepository.save(plan));
    }

    @Override
    public SubscriptionPlanListResponse getSubscriptionPlans() {
        List<SubscriptionPlanResponse> items = subscriptionPlanRepository.findAllByOrderByTenantTpCdAscBaseAmtAscIdAsc().stream()
                .map(SubscriptionPlanResponse::from)
                .toList();

        return new SubscriptionPlanListResponse(items.size(), items);
    }

    private String normalizeYn(String value, String defaultValue) {
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        return "Y".equalsIgnoreCase(value.trim()) ? "Y" : "N";
    }
}
