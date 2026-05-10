package com.sinwoo.platform.billing.repository;

import com.sinwoo.platform.billing.domain.SubscrPlan;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubscrPlanRepository extends JpaRepository<SubscrPlan, Long> {

    boolean existsByPlanCdIgnoreCase(String planCd);

    Optional<SubscrPlan> findByPlanCd(String planCd);

    List<SubscrPlan> findAllByOrderByTenantTpCdAscBaseAmtAscIdAsc();
}
