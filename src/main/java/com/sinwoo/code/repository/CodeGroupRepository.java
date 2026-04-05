package com.sinwoo.code.repository;

import com.sinwoo.code.domain.CodeGroup;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CodeGroupRepository extends JpaRepository<CodeGroup, Long> {

    boolean existsByGrpCdIgnoreCase(String grpCd);

    Optional<CodeGroup> findByGrpCdIgnoreCase(String grpCd);

    List<CodeGroup> findAllByOrderByDspOrdAscGrpCdAsc();
}
