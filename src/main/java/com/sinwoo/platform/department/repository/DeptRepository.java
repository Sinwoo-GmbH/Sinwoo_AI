package com.sinwoo.platform.dept.repository;

import com.sinwoo.platform.dept.domain.Dept;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeptRepository extends JpaRepository<Dept, Long> {

    boolean existsByTenantIdAndCoIdAndDeptCdIgnoreCase(Long tenantId, Long coId, String deptCd);

    List<Dept> findAllByTenantIdOrderByDeptLvlNoAscDeptNmAscIdAsc(Long tenantId);

    List<Dept> findAllByTenantIdAndCoIdOrderByDeptLvlNoAscDeptNmAscIdAsc(Long tenantId, Long coId);

    List<Dept> findAllByTenantIdAndCoIdInOrderByDeptLvlNoAscDeptNmAscIdAsc(Long tenantId, Collection<Long> coIds);

    Optional<Dept> findByIdAndTenantIdAndCoId(Long id, Long tenantId, Long coId);
}
