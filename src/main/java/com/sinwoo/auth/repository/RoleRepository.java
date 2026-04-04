package com.sinwoo.auth.repository;

import com.sinwoo.auth.domain.Role;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Long> {

    boolean existsByRoleCdIgnoreCase(String roleCd);

    List<Role> findAllByOrderByRoleCdAsc();

    List<Role> findByRoleCdIn(Collection<String> roleCds);

    Optional<Role> findByRoleCd(String roleCd);
}
