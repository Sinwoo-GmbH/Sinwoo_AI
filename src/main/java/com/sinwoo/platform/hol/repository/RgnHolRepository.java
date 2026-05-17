package com.sinwoo.platform.hol.repository;

import com.sinwoo.platform.hol.domain.RgnHol;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RgnHolRepository extends JpaRepository<RgnHol, Long> {

    List<RgnHol> findAllByYrAndRegionCdOrderByHolidayDtAsc(Short yr, String regionCd);

    List<RgnHol> findAllByYrAndRegionCdInOrderByHolidayDtAsc(Short yr, Collection<String> regionCds);

    List<RgnHol> findAllByHolidayDtBetweenAndRegionCdInOrderByHolidayDtAsc(
            LocalDate from, LocalDate to, Collection<String> regionCds);

    boolean existsByYrAndRegionCd(Short yr, String regionCd);
}
