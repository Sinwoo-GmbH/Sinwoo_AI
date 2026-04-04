package com.sinwoo.department.repository;

import com.sinwoo.department.domain.Department;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DepartmentRepository extends JpaRepository<Department, Long> {

    boolean existsByTenantIdAndCoIdAndDeptCdIgnoreCase(Long tenantId, Long coId, String deptCd);

    List<Department> findAllByTenantIdAndCoIdOrderByDeptLvlNoAscDeptNmAscIdAsc(Long tenantId, Long coId);

    Optional<Department> findByIdAndTenantIdAndCoId(Long id, Long tenantId, Long coId);
}
