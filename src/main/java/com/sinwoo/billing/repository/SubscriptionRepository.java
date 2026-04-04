package com.sinwoo.billing.repository;

import com.sinwoo.billing.domain.Subscription;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    List<Subscription> findAllByTenantIdOrderByCreatedAtDescIdDesc(Long tenantId);

    Optional<Subscription> findByIdAndTenantId(Long id, Long tenantId);
}
