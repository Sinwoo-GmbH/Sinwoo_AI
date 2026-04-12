package com.sinwoo.worklocation.repository;

import com.sinwoo.worklocation.domain.WorkLocation;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkLocationRepository extends JpaRepository<WorkLocation, Long> {

    boolean existsByTenantIdAndCoIdAndWorkLocCdIgnoreCase(Long tenantId, Long coId, String workLocCd);

    List<WorkLocation> findAllByTenantIdAndCoIdOrderByWorkLocNmAscIdAsc(Long tenantId, Long coId);

    Optional<WorkLocation> findByIdAndTenantIdAndCoId(Long id, Long tenantId, Long coId);
}
