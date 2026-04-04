package com.sinwoo.menu.repository;

import com.sinwoo.menu.domain.RoleMenuAuth;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleMenuAuthRepository extends JpaRepository<RoleMenuAuth, Long> {

    List<RoleMenuAuth> findAllByRoleIdOrderByMnuIdAsc(Long roleId);

    List<RoleMenuAuth> findAllByRoleIdIn(Collection<Long> roleIds);

    void deleteAllByRoleId(Long roleId);
}
