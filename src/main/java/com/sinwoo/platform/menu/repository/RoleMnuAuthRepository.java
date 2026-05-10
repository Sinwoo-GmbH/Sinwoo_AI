package com.sinwoo.platform.mnu.repository;

import com.sinwoo.platform.mnu.domain.RoleMnuAuth;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleMnuAuthRepository extends JpaRepository<RoleMnuAuth, Long> {

    List<RoleMnuAuth> findAllByRoleIdOrderByMnuIdAsc(Long roleId);

    List<RoleMnuAuth> findAllByRoleIdIn(Collection<Long> roleIds);

    void deleteAllByRoleId(Long roleId);
}
