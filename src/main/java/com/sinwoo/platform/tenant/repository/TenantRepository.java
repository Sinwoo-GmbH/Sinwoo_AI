package com.sinwoo.platform.tenant.repository;

import com.sinwoo.platform.tenant.domain.Tenant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TenantRepository extends JpaRepository<Tenant, Long> {

    boolean existsByTenantCdIgnoreCase(String tenantCd);

    boolean existsByEmlDomnIgnoreCase(String emlDomn);

    Optional<Tenant> findByTenantCdIgnoreCase(String tenantCd);

    Optional<Tenant> findByEmlDomnIgnoreCase(String emlDomn);

    List<Tenant> findAllByOrderByCreatedAtDescIdDesc();
}
