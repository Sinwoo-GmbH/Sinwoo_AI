package com.sinwoo.platform.leave.repository;

import com.sinwoo.platform.leave.domain.LeaveCoPolicy;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LeaveCoPolicyRepository extends JpaRepository<LeaveCoPolicy, Long> {

    Optional<LeaveCoPolicy> findByTenantIdAndCoIdAndDelYn(String tenantId, String coId, String delYn);
}
