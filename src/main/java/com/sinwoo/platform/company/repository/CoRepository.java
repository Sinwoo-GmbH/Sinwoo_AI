package com.sinwoo.platform.company.repository;

import com.sinwoo.platform.company.domain.Co;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CoRepository extends JpaRepository<Co, Long> {

    boolean existsByTenantIdAndCoCdIgnoreCase(Long tenantId, String coCd);

    List<Co> findAllByTenantIdOrderByCreatedAtDescIdDesc(Long tenantId);

    Optional<Co> findByIdAndTenantId(Long id, Long tenantId);
}
