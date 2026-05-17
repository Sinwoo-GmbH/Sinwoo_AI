package com.sinwoo.platform.leave.repository;

import com.sinwoo.platform.leave.domain.LeaveGrant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LeaveGrantRepository extends JpaRepository<LeaveGrant, Long> {

    Optional<LeaveGrant> findByTenantIdAndCoIdAndEmpIdAndGrantYrAndDelYn(
            String tenantId, String coId, String empId, Short grantYr, String delYn);

    List<LeaveGrant> findAllByTenantIdAndCoIdAndEmpIdAndDelYnOrderByGrantYrDesc(
            String tenantId, String coId, String empId, String delYn);

    List<LeaveGrant> findAllByTenantIdAndCoIdAndGrantYrAndDelYnOrderByEmpIdAsc(
            String tenantId, String coId, Short grantYr, String delYn);
}
