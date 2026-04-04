package com.sinwoo.auth.service;

import com.sinwoo.auth.dto.AuthProviderListResponse;
import com.sinwoo.auth.dto.AuthTokenResponse;
import com.sinwoo.auth.dto.CurrentUserResponse;
import com.sinwoo.common.security.AuthenticatedUser;
import java.util.Map;

public interface AuthService {

    AuthProviderListResponse getOauthProviders();

    AuthTokenResponse completeOauthLogin(String registrationId, String tenantCd, Map<String, Object> attributes);

    CurrentUserResponse getCurrentUser(AuthenticatedUser authenticatedUser);
}
