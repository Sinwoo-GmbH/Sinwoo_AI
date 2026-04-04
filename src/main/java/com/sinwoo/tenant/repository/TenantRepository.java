package com.sinwoo.tenant.repository;

import com.sinwoo.tenant.domain.Tenant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TenantRepository extends JpaRepository<Tenant, Long> {

    boolean existsByTenantCdIgnoreCase(String tenantCd);

    Optional<Tenant> findByTenantCdIgnoreCase(String tenantCd);

    List<Tenant> findAllByOrderByCreatedAtDescIdDesc();
}
