package com.sinwoo.platform.billing.service;

import com.sinwoo.platform.billing.domain.Subscr;
import com.sinwoo.platform.billing.domain.SubscrPlan;
import com.sinwoo.platform.billing.dto.CreateSubscrRequest;
import com.sinwoo.platform.billing.dto.SubscrListResponse;
import com.sinwoo.platform.billing.dto.SubscrResponse;
import com.sinwoo.platform.billing.repository.SubscrPlanRepository;
import com.sinwoo.platform.billing.repository.SubscrRepository;
import com.sinwoo.platform.tenant.domain.Tenant;
import com.sinwoo.platform.tenant.repository.TenantRepository;
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
public class SubscrServiceImpl implements SubscrService {

    private final SubscrRepository subscrRepository;
    private final SubscrPlanRepository subscrPlanRepository;
    private final TenantRepository tenantRepository;

    @Override
    @Transactional
    public SubscrResponse createSubscr(CreateSubscrRequest request) {
        Tenant tenant = tenantRepository.findById(request.tenantId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tenant not found"));

        SubscrPlan plan = subscrPlanRepository.findByPlanCd(request.planCd().trim().toUpperCase())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Subscription plan not found"));

        if (!tenant.getTenantTpCd().equals(plan.getTenantTpCd())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Plan tenant type does not match tenant");
        }

        String resolvedBillFreeYn = resolveBillFreeYn(tenant, request.billFreeYn());
        Subscr subscr = Subscr.create(
                tenant.getId(),
                plan.getId(),
                normalizeStatus(request.subsStsCd()),
                resolvedBillFreeYn,
                "Y".equals(resolvedBillFreeYn) ? "N" : normalizeYn(request.autoPayYn(), "Y"),
                request.strDt() == null ? LocalDate.now() : request.strDt(),
                request.endDt(),
                request.nextBillDt()
        );

        Subscr savedSubscr = subscrRepository.save(subscr);
        return SubscrResponse.from(savedSubscr, plan.getPlanCd(), plan.getPlanNm());
    }

    @Override
    public SubscrListResponse getSubscrs(Long tenantId) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tenant not found"));

        Map<Long, SubscrPlan> planById = new LinkedHashMap<>();
        subscrPlanRepository.findAll().forEach(plan -> planById.put(plan.getId(), plan));

        List<SubscrResponse> items = subscrRepository.findAllByTenantIdOrderByCreatedAtDescIdDesc(tenant.getId()).stream()
                .map(subscr -> {
                    SubscrPlan plan = planById.get(subscr.getPlanId());
                    return SubscrResponse.from(
                            subscr,
                            plan == null ? null : plan.getPlanCd(),
                            plan == null ? null : plan.getPlanNm()
                    );
                })
                .toList();

        return new SubscrListResponse(items.size(), items);
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
