package com.sinwoo.menu.repository;

import com.sinwoo.menu.domain.Menu;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MenuRepository extends JpaRepository<Menu, Long> {

    boolean existsByMnuCdIgnoreCase(String mnuCd);

    Optional<Menu> findByMnuCd(String mnuCd);

    List<Menu> findAllByOrderByMnuScopeCdAscDspOrdAscIdAsc();

    List<Menu> findAllByMnuScopeCdOrderByDspOrdAscIdAsc(String mnuScopeCd);

    List<Menu> findAllByIdInOrderByDspOrdAscIdAsc(Collection<Long> ids);
}
