package com.sinwoo.billing.support;

import com.sinwoo.billing.domain.Subscription;
import com.sinwoo.billing.domain.SubscriptionPlan;
import com.sinwoo.billing.repository.SubscriptionPlanRepository;
import com.sinwoo.billing.repository.SubscriptionRepository;
import com.sinwoo.tenant.domain.Tenant;
import com.sinwoo.tenant.repository.TenantRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BillingAccessPolicyService {

    private final TenantRepository tenantRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionPlanRepository subscriptionPlanRepository;

    public boolean hasPaidAdminAccess(Long tenantId) {
        if (tenantId == null) {
            return false;
        }

        Tenant tenant = tenantRepository.findById(tenantId).orElse(null);
        if (tenant == null) {
            return false;
        }

        if ("INTERNAL".equalsIgnoreCase(tenant.getTenantTpCd()) || "Y".equalsIgnoreCase(tenant.getBillFreeYn())) {
            return true;
        }

        Subscription subscription = subscriptionRepository.findFirstByTenantIdOrderByCreatedAtDescIdDesc(tenantId)
                .orElse(null);
        if (subscription == null) {
            return false;
        }

        if (!isEntitledStatus(subscription.getSubsStsCd())) {
            return false;
        }

        if (subscription.getEndDt() != null && subscription.getEndDt().isBefore(LocalDate.now())) {
            return false;
        }

        if ("Y".equalsIgnoreCase(subscription.getBillFreeYn())) {
            return true;
        }

        SubscriptionPlan plan = subscriptionPlanRepository.findById(subscription.getPlanId()).orElse(null);
        if (plan == null) {
            return false;
        }

        return plan.getBaseAmt() != null && plan.getBaseAmt().compareTo(BigDecimal.ZERO) > 0;
    }

    private boolean isEntitledStatus(String subsStsCd) {
        if (subsStsCd == null || subsStsCd.isBlank()) {
            return false;
        }
        return "ACTIVE".equalsIgnoreCase(subsStsCd) || "TRIAL".equalsIgnoreCase(subsStsCd);
    }
}
