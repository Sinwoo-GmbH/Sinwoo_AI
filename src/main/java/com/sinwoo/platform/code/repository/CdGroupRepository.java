package com.sinwoo.platform.code.repository;

import com.sinwoo.platform.code.domain.CdGroup;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CdGroupRepository extends JpaRepository<CdGroup, Long> {

    boolean existsByGrpCdIgnoreCase(String grpCd);

    Optional<CdGroup> findByGrpCdIgnoreCase(String grpCd);

    List<CdGroup> findAllByOrderByDspOrdAscGrpCdAsc();
}
