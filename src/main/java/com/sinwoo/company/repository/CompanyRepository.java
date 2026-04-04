package com.sinwoo.company.repository;

import com.sinwoo.company.domain.Company;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompanyRepository extends JpaRepository<Company, Long> {

    boolean existsByTenantIdAndCoCdIgnoreCase(Long tenantId, String coCd);

    List<Company> findAllByTenantIdOrderByCreatedAtDescIdDesc(Long tenantId);

    Optional<Company> findByIdAndTenantId(Long id, Long tenantId);
}
