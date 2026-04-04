package com.sinwoo.tenant.repository;

import com.sinwoo.tenant.domain.Tenant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TenantRepository extends JpaRepository<Tenant, Long> {

    boolean existsByTenantCdIgnoreCase(String tenantCd);

    List<Tenant> findAllByOrderByCreatedAtDescIdDesc();
}
