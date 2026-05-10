package com.sinwoo.platform.code.repository;

import com.sinwoo.platform.code.domain.CommonCd;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommonCdRepository extends JpaRepository<CommonCd, Long> {

    boolean existsByGrpIdAndCdIgnoreCase(Long grpId, String cd);

    Optional<CommonCd> findByGrpIdAndCdIgnoreCase(Long grpId, String cd);

    List<CommonCd> findAllByGrpIdOrderByDspOrdAscIdAsc(Long grpId);

    List<CommonCd> findAllByOrderByGrpIdAscDspOrdAscIdAsc();
}
