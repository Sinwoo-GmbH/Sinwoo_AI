package com.sinwoo.platform.worklocation.repository;

import com.sinwoo.platform.worklocation.domain.WorkLoc;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkLocRepository extends JpaRepository<WorkLoc, Long> {

    boolean existsByTenantIdAndCoIdAndWorkLocCdIgnoreCase(Long tenantId, Long coId, String workLocCd);

    List<WorkLoc> findAllByTenantIdAndCoIdOrderByWorkLocNmAscIdAsc(Long tenantId, Long coId);

    Optional<WorkLoc> findByIdAndTenantIdAndCoId(Long id, Long tenantId, Long coId);
}
