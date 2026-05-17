package com.sinwoo.platform.leave.repository;

import com.sinwoo.platform.leave.domain.LeaveReq;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LeaveReqRepository extends JpaRepository<LeaveReq, Long> {

    Optional<LeaveReq> findByIdAndTenantIdAndCoIdAndDelYn(
            Long id, String tenantId, String coId, String delYn);

    List<LeaveReq> findAllByTenantIdAndCoIdAndEmpIdAndDelYnOrderByStrDtDesc(
            String tenantId, String coId, String empId, String delYn);

    List<LeaveReq> findAllByTenantIdAndCoIdAndEmpIdAndStrDtGreaterThanEqualAndEndDtLessThanEqualAndDelYnOrderByStrDtDesc(
            String tenantId, String coId, String empId,
            LocalDate from, LocalDate to, String delYn);

    List<LeaveReq> findAllByTenantIdAndCoIdAndDelYnOrderByStrDtDesc(
            String tenantId, String coId, String delYn);
}
