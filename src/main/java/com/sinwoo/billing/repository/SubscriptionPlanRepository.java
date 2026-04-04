package com.sinwoo.billing.repository;

import com.sinwoo.billing.domain.SubscriptionPlan;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubscriptionPlanRepository extends JpaRepository<SubscriptionPlan, Long> {

    boolean existsByPlanCdIgnoreCase(String planCd);

    Optional<SubscriptionPlan> findByPlanCd(String planCd);

    List<SubscriptionPlan> findAllByOrderByTenantTpCdAscBaseAmtAscIdAsc();
}
