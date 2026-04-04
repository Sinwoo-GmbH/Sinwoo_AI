package com.sinwoo.billing.service;

import com.sinwoo.billing.domain.Subscription;
import com.sinwoo.billing.domain.SubscriptionPlan;
import com.sinwoo.billing.dto.CreateSubscriptionRequest;
import com.sinwoo.billing.dto.SubscriptionListResponse;
import com.sinwoo.billing.dto.SubscriptionResponse;
import com.sinwoo.billing.repository.SubscriptionPlanRepository;
import com.sinwoo.billing.repository.SubscriptionRepository;
import com.sinwoo.tenant.domain.Tenant;
import com.sinwoo.tenant.repository.TenantRepository;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SubscriptionServiceImpl implements SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionPlanRepository subscriptionPlanRepository;
    private final TenantRepository tenantRepository;

    @Override
    @Transactional
    public SubscriptionResponse createSubscription(CreateSubscriptionRequest request) {
        Tenant tenant = tenantRepository.findById(request.tenantId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tenant not found"));

        SubscriptionPlan plan = subscriptionPlanRepository.findByPlanCd(request.planCd().trim().toUpperCase())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Subscription plan not found"));

        if (!tenant.getTenantTpCd().equals(plan.getTenantTpCd())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Plan tenant type does not match tenant");
        }

        String resolvedBillFreeYn = resolveBillFreeYn(tenant, request.billFreeYn());
        Subscription subscription = Subscription.create(
                tenant.getId(),
                plan.getId(),
                normalizeStatus(request.subsStsCd()),
                resolvedBillFreeYn,
                "Y".equals(resolvedBillFreeYn) ? "N" : normalizeYn(request.autoPayYn(), "Y"),
                request.strDt() == null ? LocalDate.now() : request.strDt(),
                request.endDt(),
                request.nextBillDt()
        );

        Subscription savedSubscription = subscriptionRepository.save(subscription);
        return SubscriptionResponse.from(savedSubscription, plan.getPlanCd(), plan.getPlanNm());
    }

    @Override
    public SubscriptionListResponse getSubscriptions(Long tenantId) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tenant not found"));

        Map<Long, SubscriptionPlan> planById = new LinkedHashMap<>();
        subscriptionPlanRepository.findAll().forEach(plan -> planById.put(plan.getId(), plan));

        List<SubscriptionResponse> items = subscriptionRepository.findAllByTenantIdOrderByCreatedAtDescIdDesc(tenant.getId()).stream()
                .map(subscription -> {
                    SubscriptionPlan plan = planById.get(subscription.getPlanId());
                    return SubscriptionResponse.from(
                            subscription,
                            plan == null ? null : plan.getPlanCd(),
                            plan == null ? null : plan.getPlanNm()
                    );
                })
                .toList();

        return new SubscriptionListResponse(items.size(), items);
    }

    private String resolveBillFreeYn(Tenant tenant, String requestValue) {
        if ("Y".equals(tenant.getBillFreeYn())) {
            return "Y";
        }
        return normalizeYn(requestValue, "N");
    }

    private String normalizeStatus(String value) {
        if (value == null || value.isBlank()) {
            return "ACTIVE";
        }
        return value.trim().toUpperCase();
    }

    private String normalizeYn(String value, String defaultValue) {
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        return "Y".equalsIgnoreCase(value.trim()) ? "Y" : "N";
    }
}
