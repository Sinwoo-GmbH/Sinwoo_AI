package com.sinwoo.platform.wrktm.repository;

import com.sinwoo.platform.wrktm.domain.WrkTm;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WrkTmRepository extends JpaRepository<WrkTm, Long> {

    Optional<WrkTm> findByTenantIdAndCoIdAndEmpIdAndWorkDtAndDelYn(
            Long tenantId, Long coId, Long empId, LocalDate workDt, String delYn);

    List<WrkTm> findAllByTenantIdAndCoIdAndEmpIdAndWorkDtBetweenAndDelYnOrderByWorkDtAsc(
            Long tenantId, Long coId, Long empId, LocalDate from, LocalDate to, String delYn);

    List<WrkTm> findAllByTenantIdAndCoIdAndWorkDtBetweenAndDelYnOrderByEmpIdAscWorkDtAsc(
            Long tenantId, Long coId, LocalDate from, LocalDate to, String delYn);

    Optional<WrkTm> findByIdAndTenantIdAndCoId(Long id, Long tenantId, Long coId);
}
