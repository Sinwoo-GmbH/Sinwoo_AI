package com.sinwoo.business.attendance.repository;

import com.sinwoo.business.attendance.domain.HoliCache;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HoliCacheRepository extends JpaRepository<HoliCache, Long> {

    long countByCtryCdAndHoliDtBetween(String ctryCd, LocalDate startDate, LocalDate endDate);

    Optional<HoliCache> findByCtryCdAndRegionCdAndHoliDt(String ctryCd, String regionCd, LocalDate holiDt);

    List<HoliCache> findAllByCtryCdAndRegionCdInAndHoliDtBetweenOrderByHoliDtAsc(
            String ctryCd,
            Collection<String> regionCds,
            LocalDate startDate,
            LocalDate endDate
    );
}
