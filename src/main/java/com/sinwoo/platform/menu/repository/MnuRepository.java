package com.sinwoo.platform.mnu.repository;

import com.sinwoo.platform.mnu.domain.Mnu;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MnuRepository extends JpaRepository<Mnu, Long> {

    boolean existsByMnuCdIgnoreCase(String mnuCd);

    Optional<Mnu> findByMnuCd(String mnuCd);

    List<Mnu> findAllByOrderByMnuScopeCdAscDspOrdAscIdAsc();

    List<Mnu> findAllByMnuScopeCdOrderByDspOrdAscIdAsc(String mnuScopeCd);

    List<Mnu> findAllByIdInOrderByDspOrdAscIdAsc(Collection<Long> ids);
}
