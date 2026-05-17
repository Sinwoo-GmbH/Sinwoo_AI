package com.sinwoo.platform.expense.repository;

import com.sinwoo.platform.expense.domain.ExpAcc;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExpAccRepository extends JpaRepository<ExpAcc, Long> {

    List<ExpAcc> findAllByTenantIdAndCoIdAndDelYnOrderByDspOrdAscExpAccCdAsc(
            Long tenantId, Long coId, String delYn);

    Optional<ExpAcc> findByTenantIdAndCoIdAndExpAccCd(Long tenantId, Long coId, Integer expAccCd);
}
