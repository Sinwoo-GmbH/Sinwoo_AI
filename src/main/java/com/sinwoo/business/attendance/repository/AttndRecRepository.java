package com.sinwoo.business.attendance.repository;

import com.sinwoo.business.attendance.domain.AttndRec;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AttndRecRepository extends JpaRepository<AttndRec, Long> {

    Optional<AttndRec> findByTenantIdAndUsrIdAndAttndDt(Long tenantId, Long usrId, LocalDate attndDt);

    List<AttndRec> findAllByTenantIdAndUsrIdAndAttndDtBetweenOrderByAttndDtAsc(
            Long tenantId,
            Long usrId,
            LocalDate startDate,
            LocalDate endDate
    );

    List<AttndRec> findAllByTenantIdAndUsrIdAndAttndDtBetweenOrderByAttndDtDescIdDesc(
            Long tenantId,
            Long usrId,
            LocalDate startDate,
            LocalDate endDate
    );

    List<AttndRec> findAllByTenantIdAndAttndDtBetweenOrderByAttndDtDescUsrIdAscIdDesc(
            Long tenantId,
            LocalDate startDate,
            LocalDate endDate
    );

    List<AttndRec> findAllByTenantIdAndCoIdInAndAttndDtBetweenOrderByAttndDtDescUsrIdAscIdDesc(
            Long tenantId,
            Collection<Long> coIds,
            LocalDate startDate,
            LocalDate endDate
    );
}
