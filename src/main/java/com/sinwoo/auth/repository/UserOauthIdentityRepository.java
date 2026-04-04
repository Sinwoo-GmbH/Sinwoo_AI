package com.sinwoo.auth.repository;

import com.sinwoo.auth.domain.UserOauthIdentity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserOauthIdentityRepository extends JpaRepository<UserOauthIdentity, Long> {

    Optional<UserOauthIdentity> findByOauthProvCdAndOauthSub(String oauthProvCd, String oauthSub);
}
