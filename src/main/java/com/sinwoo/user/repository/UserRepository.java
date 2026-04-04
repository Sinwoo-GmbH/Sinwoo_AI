package com.sinwoo.user.repository;

import com.sinwoo.user.domain.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByTenantIdAndLgnIdIgnoreCase(Long tenantId, String lgnId);

    boolean existsByTenantIdAndEmlIgnoreCase(Long tenantId, String eml);

    Optional<User> findByTenantIdAndLgnIdIgnoreCase(Long tenantId, String lgnId);

    Optional<User> findByTenantIdAndEmlIgnoreCase(Long tenantId, String eml);

    List<User> findAllByEmlIgnoreCase(String eml);

    List<User> findAllByTenantIdOrderByCreatedAtDescIdDesc(Long tenantId);

    List<User> findAllByTenantIdAndCoIdOrderByCreatedAtDescIdDesc(Long tenantId, Long coId);
}
