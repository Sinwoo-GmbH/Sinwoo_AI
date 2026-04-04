package com.sinwoo.auth.repository;

import com.sinwoo.auth.domain.UserRole;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRoleRepository extends JpaRepository<UserRole, Long> {

    List<UserRole> findAllByUsrId(Long usrId);
}
