package com.sinwoo.platform.code.repository;

import com.sinwoo.platform.code.domain.CommonCd;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CommonCdRepository extends JpaRepository<CommonCd, Long> {

    boolean existsByGrpIdAndCdIgnoreCase(Long grpId, String cd);

    Optional<CommonCd> findByGrpIdAndCdIgnoreCase(Long grpId, String cd);

    @Query("SELECT c FROM CommonCd c WHERE c.grpId = :gid ORDER BY c.dspOrd ASC, c.id ASC")
    List<CommonCd> findByGrp(@Param("gid") Long grpId);

    @Query("SELECT c FROM CommonCd c ORDER BY c.grpId ASC, c.dspOrd ASC, c.id ASC")
    List<CommonCd> findAllSorted();
}
