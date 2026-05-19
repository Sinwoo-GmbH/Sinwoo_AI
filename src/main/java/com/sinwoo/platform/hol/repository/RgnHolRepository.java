package com.sinwoo.platform.hol.repository;

import com.sinwoo.platform.hol.domain.RgnHol;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RgnHolRepository extends JpaRepository<RgnHol, Long> {

    @Query("SELECT h FROM RgnHol h WHERE h.yr = :yr AND h.regionCd = :rgn ORDER BY h.holidayDt ASC")
    List<RgnHol> findByRegion(@Param("yr") Short yr, @Param("rgn") String rgn);

    @Query("SELECT h FROM RgnHol h WHERE h.yr = :yr AND h.regionCd IN :rgns ORDER BY h.holidayDt ASC")
    List<RgnHol> findByRegions(@Param("yr") Short yr, @Param("rgns") Collection<String> rgns);

    @Query("SELECT h FROM RgnHol h WHERE h.holidayDt BETWEEN :from AND :to AND h.regionCd IN :rgns ORDER BY h.holidayDt ASC")
    List<RgnHol> findByPeriod(@Param("from") LocalDate from, @Param("to") LocalDate to, @Param("rgns") Collection<String> rgns);

    boolean existsByYrAndRegionCd(Short yr, String regionCd);
}
