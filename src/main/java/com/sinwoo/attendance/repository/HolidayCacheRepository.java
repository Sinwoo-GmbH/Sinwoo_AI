package com.sinwoo.attendance.repository;

import com.sinwoo.attendance.domain.HolidayCache;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HolidayCacheRepository extends JpaRepository<HolidayCache, Long> {

    long countByCtryCdAndHoliDtBetween(String ctryCd, LocalDate startDate, LocalDate endDate);

    Optional<HolidayCache> findByCtryCdAndRegionCdAndHoliDt(String ctryCd, String regionCd, LocalDate holiDt);

    List<HolidayCache> findAllByCtryCdAndRegionCdInAndHoliDtBetweenOrderByHoliDtAsc(
            String ctryCd,
            Collection<String> regionCds,
            LocalDate startDate,
            LocalDate endDate
    );
}
