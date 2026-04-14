package com.sinwoo.employee.repository;

import com.sinwoo.employee.domain.Employee;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    boolean existsByTenantIdAndCoIdAndEmpNoIgnoreCase(Long tenantId, Long coId, String empNo);

    boolean existsByUsrId(Long usrId);

    Optional<Employee> findByUsrId(Long usrId);

    List<Employee> findAllByTenantIdOrderByEmpNmAscIdAsc(Long tenantId);

    List<Employee> findAllByTenantIdAndCoIdOrderByEmpNmAscIdAsc(Long tenantId, Long coId);

    List<Employee> findAllByTenantIdAndCoIdInOrderByEmpNmAscIdAsc(Long tenantId, Collection<Long> coIds);

    List<Employee> findAllByTenantIdAndCoIdAndDeptIdOrderByEmpNmAscIdAsc(Long tenantId, Long coId, Long deptId);

    Optional<Employee> findByIdAndTenantIdAndCoId(Long id, Long tenantId, Long coId);
}
