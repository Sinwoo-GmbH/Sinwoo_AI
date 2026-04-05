package com.sinwoo.code.repository;

import com.sinwoo.code.domain.CommonCode;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommonCodeRepository extends JpaRepository<CommonCode, Long> {

    boolean existsByGrpIdAndCdIgnoreCase(Long grpId, String cd);

    Optional<CommonCode> findByGrpIdAndCdIgnoreCase(Long grpId, String cd);

    List<CommonCode> findAllByGrpIdOrderByDspOrdAscIdAsc(Long grpId);

    List<CommonCode> findAllByOrderByGrpIdAscDspOrdAscIdAsc();
}
