package com.sinwoo.platform.billing.repository;

import com.sinwoo.platform.billing.domain.Subscr;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubscrRepository extends JpaRepository<Subscr, Long> {

    List<Subscr> findAllByTenantIdOrderByCreatedAtDescIdDesc(Long tenantId);

    Optional<Subscr> findFirstByTenantIdOrderByCreatedAtDescIdDesc(Long tenantId);

    Optional<Subscr> findByIdAndTenantId(Long id, Long tenantId);
}
