package com.sinwoo.platform.hol.repository;

import com.sinwoo.platform.hol.domain.CoHol;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CoHolRepository extends JpaRepository<CoHol, Long> {

    @Query("SELECT h FROM CoHol h WHERE h.tenantId = :tid AND h.coId = :cid AND h.delYn = :del ORDER BY h.strDt ASC")
    List<CoHol> findByCo(@Param("tid") Long tid, @Param("cid") Long cid, @Param("del") String del);

    @Query("""
            SELECT h FROM CoHol h
            WHERE h.tenantId = :tid AND h.coId = :cid AND h.delYn = 'N'
              AND (
                    (h.annualYn = 'Y')
                 OR (h.annualYn = 'N' AND h.applyYr = :yr)
              )
              AND h.strDt <= :to AND h.endDt >= :from
            ORDER BY h.strDt ASC
            """)
    List<CoHol> findByPeriod(
            @Param("tid") Long tid, @Param("cid") Long cid,
            @Param("yr") Short yr, @Param("from") LocalDate from, @Param("to") LocalDate to);

    @Query("SELECT h FROM CoHol h WHERE h.id = :id AND h.tenantId = :tid AND h.coId = :cid")
    Optional<CoHol> findOne(@Param("id") Long id, @Param("tid") Long tid, @Param("cid") Long cid);
}
