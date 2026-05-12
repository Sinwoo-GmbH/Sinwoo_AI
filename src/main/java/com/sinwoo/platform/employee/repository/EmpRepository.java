package com.sinwoo.platform.employee.repository;

import com.sinwoo.platform.employee.domain.Emp;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmpRepository extends JpaRepository<Emp, Long> {

    boolean existsByTenantIdAndCoIdAndEmpNoIgnoreCase(Long tenantId, Long coId, String empNo);

    boolean existsByUsrId(Long usrId);

    Optional<Emp> findByUsrId(Long usrId);

    List<Emp> findAllByTenantIdOrderByEmpNmAscIdAsc(Long tenantId);

    List<Emp> findAllByTenantIdAndCoIdOrderByEmpNmAscIdAsc(Long tenantId, Long coId);

    List<Emp> findAllByTenantIdAndCoIdInOrderByEmpNmAscIdAsc(Long tenantId, Collection<Long> coIds);

    List<Emp> findAllByTenantIdAndCoIdAndDeptIdOrderByEmpNmAscIdAsc(Long tenantId, Long coId, Long deptId);

    Optional<Emp> findByIdAndTenantIdAndCoId(Long id, Long tenantId, Long coId);
}
