package com.sinwoo.attendance.repository;

import com.sinwoo.attendance.domain.AttendanceRecord;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AttendanceRecordRepository extends JpaRepository<AttendanceRecord, Long> {

    Optional<AttendanceRecord> findByTenantIdAndUsrIdAndAttndDt(Long tenantId, Long usrId, LocalDate attndDt);

    List<AttendanceRecord> findAllByTenantIdAndUsrIdAndAttndDtBetweenOrderByAttndDtAsc(
            Long tenantId,
            Long usrId,
            LocalDate startDate,
            LocalDate endDate
    );
}
