package com.sinwoo.platform.hol.repository;

import com.sinwoo.platform.hol.domain.CoHol;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CoHolRepository extends JpaRepository<CoHol, Long> {

    List<CoHol> findAllByTenantIdAndCoIdAndDelYnOrderByStrDtAsc(
            Long tenantId, Long coId, String delYn);

    @Query("""
            SELECT h FROM CoHol h
            WHERE h.tenantId = :tenantId AND h.coId = :coId AND h.delYn = 'N'
              AND (
                    (h.annualYn = 'Y')
                 OR (h.annualYn = 'N' AND h.applyYr = :yr)
              )
              AND h.strDt <= :to AND h.endDt >= :from
            ORDER BY h.strDt ASC
            """)
    List<CoHol> findAllActiveByPeriod(
            @Param("tenantId") Long tenantId,
            @Param("coId") Long coId,
            @Param("yr") Short yr,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to);

    Optional<CoHol> findByIdAndTenantIdAndCoId(Long id, Long tenantId, Long coId);
}
