package com.sinwoo.platform.auth.repository;

import com.sinwoo.platform.auth.domain.UsrRole;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsrRoleRepository extends JpaRepository<UsrRole, Long> {

    List<UsrRole> findAllByUsrId(Long usrId);
}
