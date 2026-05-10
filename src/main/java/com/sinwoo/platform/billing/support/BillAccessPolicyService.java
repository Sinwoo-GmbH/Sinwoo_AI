package com.sinwoo.platform.billing.support;

import com.sinwoo.platform.billing.domain.Subscr;
import com.sinwoo.platform.billing.domain.SubscrPlan;
import com.sinwoo.platform.billing.repository.SubscrPlanRepository;
import com.sinwoo.platform.billing.repository.SubscrRepository;
import com.sinwoo.platform.tenant.domain.Tenant;
import com.sinwoo.platform.tenant.repository.TenantRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BillAccessPolicyService {

    private final TenantRepository tenantRepository;
    private final SubscrRepository subscrRepository;
    private final SubscrPlanRepository subscrPlanRepository;

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

        Subscr subscr = subscrRepository.findFirstByTenantIdOrderByCreatedAtDescIdDesc(tenantId)
                .orElse(null);
        if (subscr == null) {
            return false;
        }

        if (!isEntitledStatus(subscr.getSubsStsCd())) {
            return false;
        }

        if (subscr.getEndDt() != null && subscr.getEndDt().isBefore(LocalDate.now())) {
            return false;
        }

        if ("Y".equalsIgnoreCase(subscr.getBillFreeYn())) {
            return true;
        }

        SubscrPlan plan = subscrPlanRepository.findById(subscr.getPlanId()).orElse(null);
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
