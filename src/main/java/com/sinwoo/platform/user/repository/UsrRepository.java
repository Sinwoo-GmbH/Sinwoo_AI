package com.sinwoo.platform.user.repository;

import com.sinwoo.platform.user.domain.Usr;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsrRepository extends JpaRepository<Usr, Long> {

    boolean existsByTenantIdAndLgnIdIgnoreCase(Long tenantId, String lgnId);

    boolean existsByTenantIdAndEmlIgnoreCase(Long tenantId, String eml);

    Optional<Usr> findByTenantIdAndLgnIdIgnoreCase(Long tenantId, String lgnId);

    Optional<Usr> findByTenantIdAndEmlIgnoreCase(Long tenantId, String eml);

    List<Usr> findAllByEmlIgnoreCase(String eml);

    List<Usr> findAllByTenantIdOrderByCreatedAtDescIdDesc(Long tenantId);

    List<Usr> findAllByTenantIdAndCoIdOrderByCreatedAtDescIdDesc(Long tenantId, Long coId);
}
