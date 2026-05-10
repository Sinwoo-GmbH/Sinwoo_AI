package com.sinwoo.platform.auth.repository;

import com.sinwoo.platform.auth.domain.UsrOauthIdentity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsrOauthIdentityRepository extends JpaRepository<UsrOauthIdentity, Long> {

    Optional<UsrOauthIdentity> findByOauthProvCdAndOauthSub(String oauthProvCd, String oauthSub);
}
